package com.github.alex_the_nugget.taskhub.taskhub.controllers.bot;

import com.github.alex_the_nugget.taskhub.taskhub.database.DatabaseConnectionManager;
import com.github.alex_the_nugget.taskhub.taskhub.database.TaskRepository;
import com.github.alex_the_nugget.taskhub.taskhub.database.UserRepository;
import com.github.alex_the_nugget.taskhub.taskhub.services.TelegramBotService;
import com.github.alex_the_nugget.taskhub.taskhub.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Connection;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TelegramBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private static volatile TelegramBot instance;
    private final HashMap<String, Runnable> commandHandlers = new HashMap<>();
    private final HashMap<Long, String> chatStates = new HashMap<>();
    private final UserSession userSession = UserSession.getInstance();
    private final UserRepository userRepository = new UserRepository();
    private final DatabaseConnectionManager databaseConnectionManager = new DatabaseConnectionManager();
    TelegramBotService botService = new TelegramBotService();

    private TelegramBot() {
    }

    public static TelegramBot getInstance() {
        if (instance == null) {
            synchronized (TelegramBot.class) {
                if (instance == null) {
                    instance = new TelegramBot();
                }
            }
        }
        return instance;
    }

    @Override
    public String getBotUsername() {
        return "task_manager_task_bot";
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

    public void onUpdateReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        if (update.hasMessage() && update.getMessage().hasText()) {
            String inputText = update.getMessage().getText();
            SendMessage message = new SendMessage();
            message.setChatId(chatId);

            if (chatStates.containsKey(chatId) && chatStates.get(chatId).equals("awaitingCity")) {
                chatStates.remove(chatId);
                handleCityCommand(message, inputText);
            } else if (chatStates.containsKey(chatId) && chatStates.get(chatId).equals("awaitingLogin")) {
                chatStates.remove(chatId);
                handleLoginCommand(message, chatId, inputText);
            } else if (chatStates.containsKey(chatId) && chatStates.get(chatId).equals("awaitingTime")) {
                chatStates.remove(chatId);
                handleSetTimeCommand(message, chatId, inputText);
            } else {
                processCommand(inputText, message, update);
            }
        }
    }

    private void processCommand(String inputText, SendMessage message, Update update) {
        if (inputText.startsWith("/")) {
            if (update != null && update.hasMessage() && update.getMessage().hasText()) {
                String commandText = update.getMessage().getText();
                SendMessage responseMessage = new SendMessage();
                long chatId = update.getMessage().getChatId();
                responseMessage.setChatId(chatId);
                commandHandlers.put("/setlogin", () -> {
                    chatStates.put(chatId, "awaitingLogin");
                    responseMessage.setText("Введите ваш логин в системе: ");
                    executeMessage(responseMessage);
                });
                commandHandlers.put("/setcity", () -> {
                    chatStates.put(chatId, "awaitingCity");
                    responseMessage.setText("Введите название вашего города в таком формате: Asia/Yekaterinburg");
                    executeMessage(responseMessage);
                });
                commandHandlers.put("/settime", () -> {
                    chatStates.put(chatId, "awaitingTime");
                    responseMessage.setText("Введите время, в которое хотите получать рассылку в " +
                            "формате HH:mm:ss.");
                    executeMessage(responseMessage);
                });
                commandHandlers.put("/start", () -> {
                    handleStartCommand(responseMessage);
                    executeMessage(responseMessage);
                });
                commandHandlers.put("/help", () -> {
                    handleHelpCommand(responseMessage);
                    executeMessage(responseMessage);
                });
                commandHandlers.put("/mytasks", () -> {
                    handleMyTasksCommand(responseMessage);
                    executeMessage(responseMessage);
                });
                commandHandlers.put("/taskstoday", () -> {
                    handleTasksTodayCommand(responseMessage);
                    executeMessage(responseMessage);
                });
                Runnable commandHandler = commandHandlers.getOrDefault(commandText, () -> {
                    handleUnknownCommand(responseMessage);
                    executeMessage(responseMessage);
                });
                commandHandler.run();
            }
        } else {
            handleTextMessage(message);
            executeMessage(message);
        }
    }

    private void handleTextMessage(SendMessage message) {
        message.setText("Не существует такой команды");
    }

    private void handleUnknownCommand(SendMessage message) {
        message.setText("Такой команды нет. Нажмите /help");
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void handleStartCommand(SendMessage message) {
        message.setText("Привет! Я - Бот Менеджер Задач. Я необходим для того, чтобы Вы получали рассылку о " +
                "Ваших рабочих проектах и дедлайнах. Для начала укажите свой логин в системе при помощи команды /setlogin.");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("/start");
        row1.add("/help");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("/taskstoday");
        row2.add("/mytasks");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("/setlogin");
        row3.add("/setcity");
        row3.add("/settime");

        keyboardMarkup.setKeyboard(List.of(row1, row2, row3));
        message.setReplyMarkup(keyboardMarkup);
    }

    public void handleHelpCommand(SendMessage message) {
        message.setText("""
                Вот, что я умею:\s
                /start - вывести стартовое сообщение\s
                /help - вывести справочную информацию\s
                /taskstoday - показать задачи на сегодня\s
                /mytasks - показать весь список задач\s
                /setcity - установить город (для определения часового пояса)
                /settime - установить время рассылки дедлайнов\s
                """);
    }

    private void handleCityCommand(SendMessage message, String city) {
        String[] parts = city.split("/");
        String usersCity = parts[1];
        try {
            ZoneId zoneId = ZoneId.of(city);
            String usersTimeZone = zoneId.getId();
            try (Connection conn = databaseConnectionManager.connectToDB("TaskManager_DB")) {
                userRepository.updateTimeZone(conn, "SystemUsers", userSession.getLogin(), usersTimeZone);
            }
            catch(Exception e){
                log.error("e: ", e);
                return;
            }
            message.setText("Часовой пояс для вашего города успешно найден.");
            executeMessage(message);
        } catch (Exception e) {
            message.setText("Не удалось найти часовой пояс для города: " + usersCity);
            executeMessage(message);
        }
    }

    private void handleLoginCommand(SendMessage message, long chatId, String login){
        try (Connection conn = databaseConnectionManager.connectToDB("TaskManager_DB")) {
            if (userRepository.searchByLogin(conn, "SystemUsers", login)){
                userRepository.updateUserId(conn, "SystemUsers", login, chatId);
                userSession.setLogin(login);
                message.setText("Успешно! Вы найдены в системе.");
                executeMessage(message);
            }
            else{
                message.setText("Неверный логин. Вы не найдены в системе.");
                executeMessage(message);
            }
        }
        catch(Exception e){
            log.error("e: ", e);
        }
    }

    private void handleSetTimeCommand(SendMessage message, long chatId, String time) {
        if (userSession.getLogin() == null) {
            message.setText("Сначала установите ваш логин с помощью /setlogin");
            executeMessage(message);
            return;
        }

        if (!isValidTimeFormat(time)) {
            message.setText("Ошибка: некорректное время. Ожидается формат HH:mm:ss.");
            executeMessage(message);
            return;
        }

        try (Connection conn = databaseConnectionManager.connectToDB("TaskManager_DB")) {
            String timeZone = userRepository.getUserTimeZone(conn, "SystemUsers", userSession.getLogin());
            if (timeZone == null) {
                message.setText("Сначала установите ваш часовой пояс с помощью /setcity");
                executeMessage(message);
                return;
            }

            userRepository.updateNotificationTime(conn, "SystemUsers", userSession.getLogin(), time);

            scheduleDailyNotification(chatId, time, timeZone);

            message.setText("Время уведомлений успешно установлено: " + time);
        } catch(Exception e) {
            log.error("Ошибка при установке времени: ", e);
            message.setText("Произошла ошибка при установке времени уведомлений");
        }

        executeMessage(message);
    }

    private static boolean isValidTimeFormat(String time) {
        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime.parse(time, timeFormatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public void sendNewTaskNotification(String messageText, long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chatId {}: {}", chatId, e.getMessage());
        }
    }

    private void scheduleDailyNotification(long chatId, String time, String timeZoneStr) {
        ZoneId userZoneId = ZoneId.of(timeZoneStr);
        LocalTime notificationTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss"));

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    String tomorrowTasks = botService.getUserTasksDueTomorrow(userSession.getLogin());
                    SendMessage message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText(tomorrowTasks);
                    message.enableMarkdown(true);
                    execute(message);
                } catch (Exception e) {
                    log.error("Ошибка при отправке уведомления: ", e);
                }
            }
        };

        ZonedDateTime now = ZonedDateTime.now(userZoneId);
        ZonedDateTime nextNotification = now.with(notificationTime);
        if (now.compareTo(nextNotification) > 0) {
            nextNotification = nextNotification.plusDays(1);
        }
        long initialDelay = Duration.between(now, nextNotification).toMillis();

        new Timer().scheduleAtFixedRate(task, initialDelay, Duration.ofDays(1).toMillis());
    }

    private void handleMyTasksCommand(SendMessage message) {
        if (userSession.getLogin() == null) {
            message.setText("Сначала установите ваш логин с помощью /setlogin");
            return;
        }
        TelegramBotService botService = new TelegramBotService();
        String tasks = botService.getUserTasks(userSession.getLogin());
        message.setText(tasks);
        message.enableMarkdown(true);
    }

    private void handleTasksTodayCommand(SendMessage message) {
        if (userSession.getLogin() == null) {
            message.setText("Сначала установите ваш логин с помощью /setlogin");
            return;
        }
        TelegramBotService botService = new TelegramBotService();
        String tasks = botService.getUserTasksToday(userSession.getLogin());
        message.setText(tasks);
        message.enableMarkdown(true);
    }

}