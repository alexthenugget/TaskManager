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
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;

public class TelegramBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private HashMap<String, Runnable> commandHandlers = new HashMap<>();
    private HashMap<Long, String> chatStates = new HashMap<>();
    private String usersCity;
    private String userLogin;
    private String usersTimeZone;
    private DataBase userServiceDB = new DataBase();

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
                    chatStates.put(chatId, "awaitingLogin");
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
            System.out.println("Error occurred: " + e.getMessage());
        }
    }

    public void handleStartCommand(SendMessage message) {
        message.setText("Привет! Я - Бот Менеджер Задач. Я необходим для того, чтобы Вы получали рассылку о " +
                "Ваших рабочих проектах и дедлайнах. Введите свой логин в системе TaskManager:");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("/help");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("/tasktoday");
        row2.add("/mytasks");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("/setcity");
        row3.add("/settime");

        KeyboardRow row4 = new KeyboardRow();
        row4.add("/start");

        keyboardMarkup.setKeyboard(List.of(row1, row2, row3, row4));
        message.setReplyMarkup(keyboardMarkup);
    }

    public void handleHelpCommand(SendMessage message) {
        message.setText("Вот, что я умею: \n" +
                "/start - вывести стартовое сообщение \n" +
                "/help - вывести справочную информацию \n" +
                "/tasktoday - показать задачи на сегодня \n" +
                "/mytasks - показать весь список задач \n" +
                "/setcity - установить город (для определения часового пояса)\n" +
                "/settime - установить время рассылки \n");
    }

    private void handleCityCommand(SendMessage message, String city) {
        String[] parts = city.split("/");
        usersCity = parts[1];
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
            }
            else{
                message.setText("Неверный логин. Вы не найдены в системе.");
            }
        }
        catch(Exception e){
            log.error("e: ", e);
        }
    }

}
