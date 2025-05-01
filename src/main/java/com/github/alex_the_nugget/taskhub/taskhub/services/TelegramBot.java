package com.github.alex_the_nugget.taskhub.taskhub.services;

import com.github.alex_the_nugget.taskhub.taskhub.database.DataBase;
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
    private final HashMap<String, Runnable> commandHandlers = new HashMap<>();
    private final HashMap<Long, String> chatStates = new HashMap<>();
    private String userLogin;
    private String usersTimeZone;
    private final DataBase userServiceDB = new DataBase();

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
        row2.add("/tasktoday");
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
                /tasktoday - показать задачи на сегодня\s
                /mytasks - показать весь список задач\s
                /setcity - установить город (для определения часового пояса)
                /settime - установить время рассылки\s
                """);
    }

    private void handleCityCommand(SendMessage message, String city) {
        String[] parts = city.split("/");
        String usersCity = parts[1];
        try {
            ZoneId zoneId = ZoneId.of(city);
            usersTimeZone = zoneId.getId();
            try (Connection conn = userServiceDB.connectToDB("TaskManager_DB")) {
                userServiceDB.updateCity(conn, "SystemUsers", userLogin, city);
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
        try (Connection conn = userServiceDB.connectToDB("TaskManager_DB")) {
            if (userServiceDB.searchByLogin(conn, "SystemUsers", login)){
                userServiceDB.updateUserId(conn, "SystemUsers", login, chatId);
                userLogin = login;
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

    private void handleSetTimeCommand(SendMessage message, long chatId, String time){
        if (!isValidTimeFormat(time)) {
            message.setText("Ошибка: некорректное время. Ожидается формат HH:mm:ss.");
            return;
        }
        ZoneId userZoneId = ZoneId.of(time);
        LocalDateTime now = LocalDateTime.now(userZoneId);
        LocalTime parsedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalDateTime notificationTime = LocalDateTime.of(now.toLocalDate(), parsedTime);

        if (now.isAfter(notificationTime)) {
            notificationTime = notificationTime.plusDays(1);
        }

        ZonedDateTime zonedNow = ZonedDateTime.now(userZoneId);
        ZonedDateTime zonedTargetTime = notificationTime.atZone(userZoneId);

        long delay = Duration.between(zonedNow, zonedTargetTime).toMillis();
        long period = Duration.ofDays(1).toMillis();

        Timer timer = new Timer();
        String response = "Task";
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                message.setText(response);
                executeMessage(message);
            }
        };
        timer.scheduleAtFixedRate(timerTask, delay, period);
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

}
