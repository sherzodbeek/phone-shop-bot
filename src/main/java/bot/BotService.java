package bot;

import model.User;
import model.enums.BotState;
import model.enums.Language;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BotService {
    public static User hasUser(Update update) throws SQLException, ClassNotFoundException {
        Long chatId = update.getCallbackQuery() != null ? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId();
        org.telegram.telegrambots.meta.api.objects.User tgUSer=update.getCallbackQuery()!=null?update.getCallbackQuery().getFrom():update.getMessage().getFrom();
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/phone_shop",
                "postgres",
                "123"
        );
        String sql = "select * from users where tg_user_id=" + chatId;
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        User user = new User();

        if (resultSet.next()) {
            user.setId(resultSet.getInt(1));
            user.setName(resultSet.getString(2));
            user.setPhoneNumber(resultSet.getString(3));
            user.setTgUserId(resultSet.getLong(6));
            user.setLanguage(resultSet.getString(7) != null ? Language.valueOf(resultSet.getString(7)) : Language.NOT_SELECTED);
            user.setBotState(resultSet.getString(8) != null ? BotState.valueOf(resultSet.getString(8)) : BotState.CHOOSE_LANG);
        } else {
            String name =update.getCallbackQuery()!=null?update.getCallbackQuery().getFrom().getFirstName() : update.getMessage().getFrom().getFirstName();
            String phoneNumber = "+9989xyyyyyyy";
            String reGSql = "INSERT INTO users (name, phone_number, tg_user_id, language, bot_state) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(reGSql);
            ps.setString(1, name);
            ps.setString(2, phoneNumber);
            ps.setLong(3, chatId);
            ps.setString(4, String.valueOf(Language.NOT_SELECTED));
            ps.setString(5, String.valueOf(BotState.CHOOSE_LANG));
            ps.executeUpdate();
            ps.close();
            user.setName(name);
            user.setTgUserId(chatId);
            user.setLanguage(Language.NOT_SELECTED);
            user.setBotState(BotState.CHOOSE_LANG);
        }
        resultSet.close();
        statement.close();
        connection.close();
        return user;
    }

    public static SendMessage start(Update update) throws SQLException, ClassNotFoundException {
        SendMessage sendMessage = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        User user = hasUser(update);
        sendMessage.setText("Tilni tanlang\nBыберите язык");

        InlineKeyboardMarkup markup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> button=new ArrayList<>();
        List<InlineKeyboardButton> rowFirst=new ArrayList<>();
        InlineKeyboardButton button1=new InlineKeyboardButton()
                .setText("UZ")
                .setCallbackData("uz");
        InlineKeyboardButton button2=new InlineKeyboardButton()
                .setText("RU")
                .setCallbackData("ru");
        rowFirst.add(button1);
        rowFirst.add(button2);

        button.add(rowFirst);
        markup.setKeyboard(button);

        sendMessage.setReplyMarkup(markup);
        sendMessage.setChatId(chatId);
        return sendMessage;
    }

    public static void addProduct() throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/phone_shop",
                "postgres",
                "123");
        for (int i = 1; i <=10 ; i++) {
            String path = "D:\\Bootcamp\\Database\\Phone_Shop_Bot\\src\\main\\resources\\phone" + i + ".jpg";
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);


            PreparedStatement ps = c.prepareStatement("INSERT INTO attachment(name, size, type) VALUES (?, ?, ?)");
            String name1 = file.getName();
            String name = name1.substring(0, name1.indexOf('.'));
            String type = name1.substring(name1.indexOf('.') + 1);
            ps.setString(1, name);
            ps.setLong(2, file.length());
            ps.setString(3, type);
            ps.executeUpdate();
            ps.close();

            PreparedStatement ps1 = c.prepareStatement("INSERT INTO attachment_content(attachment_id, bytes) VALUES (?, ?)");
            ps1.setInt(1, i);
            ps1.setBinaryStream(2, fis, (int) file.length());
            ps1.executeUpdate();
            ps1.close();

            PreparedStatement ps2 = c.prepareStatement("INSERT INTO product(name_uz, name_ru, description_uz, description_ru, income_price, sale_price, attachment_id ) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps2.setString(1, "Telefon");
            ps2.setString(2, "Тел");
            ps2.setString(3, "Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).");
            ps2.setString(4, "С годами появились разные версии, иногда случайно, иногда специально (с добавлением юмора и т.п.).");
            ps2.setDouble(5, i*111.11);
            ps2.setDouble(6, i*122.11);
            ps2.setInt(7, i);
            ps2.executeUpdate();
            ps2.close();
            fis.close();
        }
        c.close();
    }

    public static SendMessage setLang(Update update) throws SQLException, ClassNotFoundException {
        SendMessage message=new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        message.setChatId(chatId);
        String text = update.getCallbackQuery().getData();
        User user = hasUser(update);
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/phone_shop",
                "postgres",
                "123"
        );
        String sql = "UPDATE users SET language=?, bot_state=? WHERE tg_user_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, text.equals("uz")? String.valueOf(Language.UZ): String.valueOf(Language.RU));
        ps.setString(2, String.valueOf(BotState.SHARE_CONTACT));
        ps.setLong(3, user.getTgUserId());
        ps.executeUpdate();
        ps.close();
        connection.close();

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setSelective(true);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);
        List<KeyboardRow> row = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton(text.equals("uz")?"Raqamni bo'lishish":"Поделиться контактом").setRequestContact(true));
        row.add(keyboardRow);
        keyboard.setKeyboard(row);
        message.setText(text.equals("uz")?"Raqaminggizni yuboring":"Отправить контакт");
        message.setReplyMarkup(keyboard);
        return message;
    }

    public static SendMessage setContact(Update update){
        Long chatId = update.getMessage().getChatId();


    }

}
