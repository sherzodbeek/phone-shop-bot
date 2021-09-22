package model;

import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

public class Main extends TelegramLongPollingBot {
    public static void main(String[] args) throws TelegramApiRequestException {
        Connection c;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/postgres",
                            "postgres","123");
//            String path = "D:\\Bootcamp\\Database\\Phone_Shop_Bot\\src\\main\\resources\\gifbu.mp4";
//            File file = new File(path);
//            FileInputStream fis = new FileInputStream(file);
//            PreparedStatement ps = c.prepareStatement("INSERT INTO images VALUES (?, ?)");
//            ps.setString(1, file.getName());
//            ps.setBinaryStream(2, fis, (int)file.length());
//            ps.executeUpdate();
//            ps.close();
//            fis.close();
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        telegramBotsApi.registerBot(new Main());
    }

    @Override
    public String getBotUsername() {
        return "@phone_shop_uz_bot";
    }

    @Override
    public String getBotToken() {
        return "1766684680:AAH-Xt9-YbdmdGtgOQ_OXDo7pliXTtuI-as";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.getMessage().getText().equals("/start")) {
            long chatId = update.getMessage().getChatId();
            Connection c;
            try {
                Class.forName("org.postgresql.Driver");
                c = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/postgres",
                                "postgres","123");

                PreparedStatement ps = c.prepareStatement("SELECT img FROM images LIMIT 1");
                ResultSet rs = ps.executeQuery();
                ByteArrayInputStream inputStream = null;

                while (rs.next()) {
                    byte[] imgBytes = rs.getBytes(1);
                    inputStream = new ByteArrayInputStream(imgBytes);
                }
                rs.close();
                ps.close();
                SendPhoto message = new SendPhoto().setPhoto("Bu rasm", inputStream);
                message.setCaption("Redmi 7");
                message.setChatId(chatId);
                execute(message);



            } catch (TelegramApiException | SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
}
