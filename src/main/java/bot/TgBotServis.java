package bot;


import lombok.SneakyThrows;
import model.*;
import model.enums.BotState;
import model.enums.Language;
import model.enums.OrderStatus;
import org.checkerframework.checker.units.qual.C;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import sun.util.resources.LocaleData;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;

public class TgBotServis {

    public static final String url = "jdbc:postgresql://localhost:5432/phone_shop?user=postgres&password=123";

    public static User hasUser(Update update) throws SQLException, ClassNotFoundException {
        Long chatId = update.getCallbackQuery() != null ? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId();
        org.telegram.telegrambots.meta.api.objects.User tgUSer = update.getCallbackQuery() != null ? update.getCallbackQuery().getFrom() : update.getMessage().getFrom();
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(TgBotServis.url);
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
            user.setPageNum(resultSet.getInt(9));
            user.setOldMessageId(resultSet.getInt(10));
            user.setLastPageNum(resultSet.getInt(11));
            user.setProductId(resultSet.getInt(12));
            user.setAmount(resultSet.getInt(13));
        } else {
            String name = update.getCallbackQuery() != null ? update.getCallbackQuery().getFrom().getFirstName() : update.getMessage().getFrom().getFirstName();
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
        return user;
    }

    @SneakyThrows
    public static void changeBotState(User user, BotState botState) {
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(TgBotServis.url);
        String sql = "UPDATE users SET bot_state=? WHERE tg_user_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, String.valueOf(botState));
        ps.setLong(2, user.getTgUserId());
        ps.executeUpdate();
        ps.close();
        connection.close();
    }

    public static String checkPhoneNumber(String phone) {
        phone = phone.replace(" ", "");
        for (int i = 0; i < phone.length(); i++) {
            if (Character.isLetter(phone.charAt(i))) {
                return null;
            }
        }
        if (phone.length() == 9) {
            for (int i = 0; i < phone.length(); i++) {
                if (!Character.isDigit(phone.charAt(i))) {
                    return null;
                }
            }
            return "+998" + phone;
        } else if (phone.length() == 12) {
            if (phone.startsWith("998")) {
                for (int i = 0; i < phone.length(); i++) {
                    if (!Character.isDigit(phone.charAt(i))) {
                        return null;
                    }
                }
                phone = "+" + phone;
                return phone;
            } else {
                return null;
            }
        } else if (phone.length() == 13) {
            if (phone.startsWith("+998")) {
                for (int i = 1; i < phone.length(); i++) {
                    if (!Character.isDigit(phone.charAt(i))) {
                        return null;
                    }
                }
                return phone;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static SendMessage start(Update update) throws SQLException, ClassNotFoundException {
        SendMessage sendMessage = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        User user = hasUser(update);
        changeBotState(user, BotState.CHOOSE_LANG);
        sendMessage.setText("*Tilni tanlang*\n*Bыберите язык*\uD83D\uDC47\uD83C\uDFFB");
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> button = new ArrayList<>();
        List<InlineKeyboardButton> rowFirst = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton()
                .setText("  UZ\uD83C\uDDFA\uD83C\uDDFF  ")
                .setCallbackData("uz");
        InlineKeyboardButton button2 = new InlineKeyboardButton()
                .setText("  RU\uD83C\uDDF7\uD83C\uDDFA  ")
                .setCallbackData("ru");
        rowFirst.add(button1);
        rowFirst.add(button2);
        button.add(rowFirst);
        markup.setKeyboard(button);
        sendMessage.setReplyMarkup(markup);
        sendMessage.setChatId(chatId);
        return sendMessage;
    }

    @SneakyThrows
    public static SendMessage setLang(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        message.setChatId(chatId);
        String text = update.getCallbackQuery().getData();
        User user = hasUser(update);
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(TgBotServis.url);
        String sql = "UPDATE users SET language=?, bot_state=? WHERE tg_user_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, text.equals("uz") ? String.valueOf(Language.UZ) : String.valueOf(Language.RU));
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
        keyboardRow.add(new KeyboardButton(text.equals("uz") ? "Raqamni bo'lishish" : "Поделиться контактом").setRequestContact(true));
        row.add(keyboardRow);
        keyboard.setKeyboard(row);
        message.setText(text.equals("uz") ? "*Raqaminggizni yuboring (+998 XX XXX XX XX)*" : "*Отправить контакт (+998 XX XXX XX XX)*");
        message.setParseMode(ParseMode.MARKDOWN);
        message.setReplyMarkup(keyboard);
        return message;
    }

    @SneakyThrows
    public static SendMessage setContact(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setParseMode(ParseMode.MARKDOWN);
        String phone = update.getMessage().hasContact() ? update.getMessage().getContact().getPhoneNumber() : update.getMessage().getText();
        String s = checkPhoneNumber(phone);
        User user = hasUser(update);
        String lang = user.getLanguage().name();
        if (s != null) {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(TgBotServis.url);
            String sql = "UPDATE users SET phone_number=?, bot_state=? WHERE tg_user_id=?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, s);
            ps.setString(2, String.valueOf(BotState.SHARE_NAME));
            ps.setLong(3, user.getTgUserId());
            ps.executeUpdate();
            ps.close();
            connection.close();
            message.setText(lang.equals("UZ") ? "Ismingiz to'grimi:  *" + user.getName()+ "*": "Правильноли указан ваша имя:  *" + user.getName()+ "*");
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> button = new ArrayList<>();
            List<InlineKeyboardButton> rowFirst = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton()
                    .setText(lang.equals("UZ") ? "  Yo'q ❌  " : "  Нет ❌  ")
                    .setCallbackData("errorName");
            InlineKeyboardButton button2 = new InlineKeyboardButton()
                    .setText(lang.equals("UZ") ? "  Ha ✅  " : "  Да ✅  ")
                    .setCallbackData("correctName");
            rowFirst.add(button1);
            rowFirst.add(button2);
            button.add(rowFirst);
            markup.setKeyboard(button);
            message.setReplyMarkup(markup);
        } else {
            message.setText(lang.equals("UZ") ? "Telefon raqamingizni noto'g'ri kiritdingiz. Iltimos, telefon raqamingizni quyidagi formatda jonating : *(+998 XX XXX XX XX)*" : "Телефон номер неправилно отправлен.Пожалуйста отправьте номер в нужном формате: *(+998 XX XXX XX XX)*");
            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
            keyboard.setSelective(true);
            keyboard.setResizeKeyboard(true);
            keyboard.setOneTimeKeyboard(true);
            List<KeyboardRow> row = new ArrayList<>();
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(new KeyboardButton(lang.equals("UZ") ? "Raqamni bo'lishish" : "Поделиться контактом").setRequestContact(true));
            row.add(keyboardRow);
            keyboard.setKeyboard(row);
            message.setReplyMarkup(keyboard);
        }
        return message;
    }

    @SneakyThrows
    public static SendMessage setOrAskName(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        message.setChatId(chatId);
        message.setParseMode(ParseMode.MARKDOWN);
        String data = update.getCallbackQuery().getData();
        User user = hasUser(update);
        String lang = user.getLanguage().name();
        if (data.startsWith("error")) {
            message.setText(lang.equals("UZ") ? "Ismingizni kiriting : (Ketmon)" : "Введите имя : (Теша)");
        } else {
            changeBotState(user, BotState.SHOW_MENU);
            message.setText(lang.equals("UZ") ? "*Bo'limni tanlang*\uD83D\uDC47\uD83C\uDFFB" : "*Виберите раздел*\uD83D\uDC47\uD83C\uDFFB");
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton()
                    .setText(lang.equals("UZ") ? "Buyurtma berish\uD83D\uDCF1" : "Заказать\uD83D\uDCF1")
                    .setCallbackData("startOrder");
            row.add(button);
            button = new InlineKeyboardButton()
                    .setText(lang.equals("UZ") ? "Savatcha\uD83D\uDED2" : "Карзина\uD83D\uDED2")
                    .setCallbackData("showBasket");
            row.add(button);
            rowList.add(row);
            row = new ArrayList<>();
            button = new InlineKeyboardButton()
                    .setText(lang.equals("UZ")?"Buyurtmalarni ko'rish\uD83D\uDD0D":"Посмотреть заказы\uD83D\uDD0D")
                    .setCallbackData("showOrders");
            row.add(button);
            rowList.add(row);
            row = new ArrayList<>();
            button = new InlineKeyboardButton()
                    .setText(lang.equals("UZ") ? "Sozlamalar⚙️" : "Настройки⚙️")
                    .setCallbackData("settings");
            row.add(button);
            button = new InlineKeyboardButton()
                    .setText(lang.equals("UZ") ? "Biz haqimizdaℹ️" : "О насℹ️")
                    .setCallbackData("aboutUs");
            row.add(button);
            rowList.add(row);
            markup.setKeyboard(rowList);
            message.setReplyMarkup(markup);
        }
        return message;
    }

    @SneakyThrows
    public static SendMessage updateName(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        User user = hasUser(update);
        String lang = user.getLanguage().name();
        changeBotState(user, BotState.SHOW_MENU);
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(TgBotServis.url);
        String sql = "UPDATE users SET name=? WHERE tg_user_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, String.valueOf(update.getMessage().getText()));
        ps.setLong(2, user.getTgUserId());
        ps.executeUpdate();
        ps.close();
        connection.close();
        message.setText(lang.equals("UZ") ? "*Bo'limni tanlang*\uD83D\uDC47\uD83C\uDFFB" : "*Виберите раздел*\uD83D\uDC47\uD83C\uDFFB");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Buyurtma berish\uD83D\uDCF1" : "Заказать\uD83D\uDCF1")
                .setCallbackData("startOrder");
        row.add(button);
        button = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Savatcha\uD83D\uDED2" : "Карзина\uD83D\uDED2")
                .setCallbackData("showBasket");
        row.add(button);
        rowList.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton()
                .setText(lang.equals("UZ")?"Buyurtmalarni ko'rish\uD83D\uDD0D":"Посмотреть заказы\uD83D\uDD0D")
                .setCallbackData("showOrders");
        row.add(button);
        rowList.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Sozlamalar⚙️" : "Настройки⚙️")
                .setCallbackData("settings");
        row.add(button);
        button = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Biz haqimizdaℹ️" : "О насℹ️")
                .setCallbackData("aboutUs");
        row.add(button);
        rowList.add(row);
        markup.setKeyboard(rowList);
        message.setReplyMarkup(markup);
        return message;
    }


    @SneakyThrows
    public static SendPhoto aboutUs(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        User user = hasUser(update);
        changeBotState(user, BotState.SHOW_INFO);
        String lang = user.getLanguage().name();
        String path = "D:\\Bootcamp\\Database\\Telefon shop final\\TG_G10_bot\\src\\main\\resources\\aboutUs.png";
        File file = new File(path);
        SendPhoto sendPhoto = new SendPhoto()
                .setPhoto(file)
                .setCaption(lang.equals("UZ") ? "UZ:>>Lorem Ipsum is simply dummy text of the printing and typesetting industry" :
                        "RU:>>Lorem Ipsum is simply dummy text of the printing and typesetting industry");
        sendPhoto.setReplyMarkup(backToMainButtonMarkup(update));
        sendPhoto.setChatId(chatId);
        return sendPhoto;
    }


    public static ReplyKeyboardMarkup backToMainButtonMarkup(Update update) throws SQLException, ClassNotFoundException {
        User user = hasUser(update);
        String lang = user.getLanguage().name();
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setSelective(true);
        keyboard.setResizeKeyboard(true);
        List<KeyboardRow> row = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton(lang.equals("UZ") ? "Bosh menuga qaytish◀️" : "Назад в главный меню◀️"));
        row.add(keyboardRow);
        keyboard.setKeyboard(row);
        return keyboard;
    }

    @SneakyThrows
    public static SendMessage backShowMainMenu(Update update) {
        SendMessage sendMessage = new SendMessage();
        Long chatId = update.getCallbackQuery()!=null?update.getCallbackQuery().getMessage().getChatId():update.getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove());
        User user = hasUser(update);
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        changeBotState(user, BotState.SHOW_MENU);
        String lang = user.getLanguage().name();
        sendMessage.setText(lang.equals("UZ") ? "*Bo'limni tanlang*\uD83D\uDC47\uD83C\uDFFB" : "*Виберите раздел*\uD83D\uDC47\uD83C\uDFFB");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Buyurtma berish\uD83D\uDCF1" : "Заказать\uD83D\uDCF1")
                .setCallbackData("startOrder");
        row.add(button);
        button = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Savatcha\uD83D\uDED2" : "Карзина\uD83D\uDED2")
                .setCallbackData("showBasket");
        row.add(button);
        rowList.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton()
                .setText(lang.equals("UZ")?"Buyurtmalarni ko'rish\uD83D\uDD0D":"Посмотреть заказы\uD83D\uDD0D")
                .setCallbackData("showOrders");
        row.add(button);
        rowList.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Sozlamalar⚙️" : "Настройки⚙️")
                .setCallbackData("settings");
        row.add(button);
        button = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Biz haqimizdaℹ️" : "О насℹ️")
                .setCallbackData("aboutUs");
        row.add(button);
        rowList.add(row);
        markup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(markup);
        return sendMessage;
    }

    @SneakyThrows
    public static SendMessage showSettings(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        User user = hasUser(update);
        changeBotState(user, BotState.EDIT_SETTINGS);
        String lang = user.getLanguage().name();
        message.setChatId(chatId);
        message.setText(lang.equals("UZ") ? "Nima o'zgartirmoqchisiz ?" : "Что вы хотите изменить ?");
        Map<String, String> map=new LinkedHashMap<>();
        map.put(lang.equals("UZ")?"Ismni o'zgartirish":"Изменить имя", "changeName");
        map.put(lang.equals("UZ")?"Telefon raqamini o'zgartirish":"Изменить номер телефона", "changePhone");
        map.put(lang.equals("UZ")?"Tilni o'zgartirish" : "Изменить язык", "changeLanguage");
        map.put(lang.equals("UZ")?"Orqaga " : "Назад", "backToMain");
        message.setReplyMarkup( generateInline(map, 3));

        return message;


    }

    @SneakyThrows
    public static SendMessage change(Update update, String type) {
        SendMessage message = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        User user = hasUser(update);
        message.setChatId(chatId);
        if (type.equals("name")){
            changeBotState(user, BotState.CHANGE_NAME);
            message.setText(user.getLanguage().name().equals("UZ") ?
                    "Ismingizni kiriting" : "Введите имя");
        }
        else if (type.equals("phone"))  {
            changeBotState(user, BotState.CHANGE_PHONE);
            message.setText(user.getLanguage().name().equals("UZ") ?
                    "Telefon raqamingizni kiriting" : "Введите номер телефона");
        }
        else if (type.equals("lang"))  {
            changeBotState(user, BotState.CHANGE_LANG);
            message.setText(user.getLanguage().name().equals("UZ") ?
                    "Til tanlang" : "Выберите язык");
            Map<String, String> map=new LinkedHashMap<>();
            map.put(user.getLanguage().name().equals("UZ") ?"UZ\uD83C\uDDFA\uD83C\uDDFF":"УЗ\uD83C\uDDFA\uD83C\uDDFF", "uz");
            map.put(user.getLanguage().name().equals("UZ") ?"RU\uD83C\uDDF7\uD83C\uDDFA":"РУ\uD83C\uDDF7\uD83C\uDDFA", "ru");
            message.setReplyMarkup( generateInline(map, 2));
        }
        return message;

    }

    @SneakyThrows
    public static void changeSettings(User user, String data) {
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(TgBotServis.url);
        String parametr = "";
        switch (user.getBotState().name()) {
            case "CHANGE_NAME":
                parametr = "name";
                break;
            case "CHANGE_PHONE":
                parametr = "phone_number";
                break;
            case "CHANGE_LANG":
                parametr = "language";
                break;
        }
        String sql = "UPDATE users SET " + parametr + "=? WHERE tg_user_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, data);
        ps.setLong(2, user.getTgUserId());
        ps.executeUpdate();
        ps.close();
        connection.close();
    }

    static int counter = 0;
    static List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
    static List<InlineKeyboardButton> row = new ArrayList<>();

    public static InlineKeyboardMarkup generateInline(Map<String, String> map, int columnAmount) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        map.forEach((s, s2) -> {
            row.add(new InlineKeyboardButton().setText(s).setCallbackData(s2));

            counter++;
            if (counter % columnAmount == 0) {
                rowList.add(row);
                row = new ArrayList<>();
            }
        });
        if (counter % columnAmount != 0) {
            rowList.add(row);
        }
        markup.setKeyboard(rowList);
        counter = 0;
        rowList = new ArrayList<>();
        row = new ArrayList<>();
        return markup;
    }

    @SneakyThrows
    public static SendMessage getChange(Update update) {
        SendMessage message = new SendMessage();
        String data = update.getCallbackQuery()!=null?update.getCallbackQuery().getData():update.getMessage().getText();
        Long chatId = update.getCallbackQuery()!=null?update.getCallbackQuery().getMessage().getChatId():update.getMessage().getChatId();
        User user = hasUser(update);
        message.setChatId(chatId);
        if (user.getBotState().equals(BotState.CHANGE_PHONE)){
            String s = checkPhoneNumber(data);
            if (s==null){
                message.setText(user.getLanguage().name().equals("UZ")?"To'g'ri raqam kiriting":"Наберите правилний номер");
                return message;
            } else {
                changeSettings(user, s);
            }

        }
        String lang = user.getLanguage().name();
        if (user.getBotState().equals(BotState.CHANGE_LANG)){
            data=data.toUpperCase();
            lang=data;
            changeSettings(user, data);
        }
        if(user.getBotState().equals(BotState.CHANGE_NAME)) {
            changeSettings(user, data);
        }
        message.setText(lang.equals("UZ") ? "Nima o'zgartirmoqchisiz ?" : "Что вы хотите изменить ?");
        Map<String, String> map=new LinkedHashMap<>();
        map.put(lang.equals("UZ")?"Ismni o'zgartirish":"Изменить имя", "changeName");
        map.put(lang.equals("UZ")?"Telefon raqamini o'zgartirish":"Изменить номер телефона", "changePhone");
        map.put(lang.equals("UZ")?"Tilni o'zgartirish" : "Изменить язык", "changeLanguage");
        map.put(lang.equals("UZ")?"Orqaga " : "Назад", "backToMain");
        message.setReplyMarkup( generateInline(map, 3));
        return message;
    }

    @SneakyThrows
    public static SendMessage removeKeyboard(Update update) {
        SendMessage message = new SendMessage();
        User user = hasUser(update);
        Long chatId = update.getCallbackQuery() != null ? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId();
        message.setText(user.getLanguage().name().equals("UZ")?"Davom etamiz":"Мы продолжим");
        message.setChatId(chatId);
        ReplyKeyboardRemove remove = new ReplyKeyboardRemove().setSelective(false);
        message.setReplyMarkup(remove);
        return message;
    }

    @SneakyThrows
    public static SendMessage removeKeyboardMainMenu(Update update) {
        SendMessage message = new SendMessage();
        User user = hasUser(update);
        message.setParseMode(ParseMode.MARKDOWN);
        Long chatId = update.getCallbackQuery() != null ? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId();
        message.setText(user.getLanguage().name().equals("UZ")?"*Bosh menyu*":"*Главное меню*");
        message.setChatId(chatId);
        ReplyKeyboardRemove remove = new ReplyKeyboardRemove().setSelective(false);
        message.setReplyMarkup(remove);
        return message;
    }

    @SneakyThrows
    public static SendMessage startOrder(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        message.setChatId(chatId);
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(TgBotServis.url);

        String sql3 = "UPDATE users SET page_num=? WHERE tg_user_id=?";
        PreparedStatement ps3 = connection.prepareStatement(sql3);
        ps3.setInt(1, 1);
        ps3.setLong(2, chatId);
        ps3.executeUpdate();
        ps3.close();

        User user = hasUser(update);
        changeBotState(user, BotState.START_ORDER);
        String lang = user.getLanguage().name();

        int numProducts = 0;
        String sql1 = "select count(id) from product";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql1);
        if(rs.next()) {
            numProducts = rs.getInt(1);
        }
        st.close();
        rs.close();
        int lastPage = 0;
        if(numProducts%10==0) {
          lastPage = numProducts/10;
        } else {
            lastPage = (numProducts/10)+1;
        }
        String sql2 = "UPDATE users SET last_page_num=? WHERE tg_user_id=?";
        PreparedStatement ps2 = connection.prepareStatement(sql2);
        ps2.setInt(1, lastPage);
        ps2.setLong(2, user.getTgUserId());
        ps2.executeUpdate();
        ps2.close();


        String sql = "select id, name_uz from product limit 10";
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(sql);
        resultSet.last();
        int rowNum = resultSet.getRow();
        resultSet.beforeFirst();
        String product_names = "";

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> rowFirst = new ArrayList<>();
        List<InlineKeyboardButton> rowSecond = new ArrayList<>();
        List<InlineKeyboardButton> rowThird = new ArrayList<>();
        for (int i = 1; i <=10; i++) {
            if (resultSet.next()) {
                product_names += i + ") " + resultSet.getString(2) + "\n";
                if (i <= 5) {
                    InlineKeyboardButton button = new InlineKeyboardButton()
                            .setText(String.valueOf(i))
                            .setCallbackData("productId_" + resultSet.getInt(1));
                    rowFirst.add(button);
                } else {
                    InlineKeyboardButton button1 = new InlineKeyboardButton()
                            .setText(String.valueOf(i))
                            .setCallbackData("productId_" + resultSet.getInt(1));
                    rowSecond.add(button1);
                }
            }
        }
        InlineKeyboardButton back = new InlineKeyboardButton()
                .setText(lang.equals("UZ")?"Orqaga\uD83D\uDD19":"Назад\uD83D\uDD19")
                .setCallbackData("/back");
        InlineKeyboardButton next = new InlineKeyboardButton()
                .setText(lang.equals("UZ")?"Oldinga\uD83D\uDD1C":"Далее\uD83D\uDD1C")
                .setCallbackData("/next");
        rowThird.add(back);
        rowThird.add(next);
        List<InlineKeyboardButton> rowFourth = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Savatcha\uD83D\uDED2" : "Карзина\uD83D\uDED2")
                .setCallbackData("showBasket");
        rowFourth.add(button);
        List<InlineKeyboardButton> rowFifth = new ArrayList<>();
        InlineKeyboardButton mainMenu = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Bosh menyu◀️" : "Главный меню◀️")
                .setCallbackData("backToMain");
        rowFifth.add(mainMenu);
        buttons.add(rowFirst);
        buttons.add(rowSecond);
        buttons.add(rowThird);
        buttons.add(rowFourth);
        buttons.add(rowFifth);
        markup.setKeyboard(buttons);
        message.setText(product_names);
        message.setReplyMarkup(markup);
        return message;
    }

    @SneakyThrows
    public static SendMessage nextPage(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        message.setChatId(chatId);
        User user = hasUser(update);
        int pageNum = user.getPageNum();
        String lang = user.getLanguage().name();
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(TgBotServis.url);
        String sql = "select id, name_uz from product limit 10 offset "+(pageNum * 10);
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(sql);
        resultSet.last();
        int rowNum = resultSet.getRow();
        resultSet.beforeFirst();
        String product_names = "";

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> rowFirst = new ArrayList<>();
        List<InlineKeyboardButton> rowSecond = new ArrayList<>();
        List<InlineKeyboardButton> rowThird = new ArrayList<>();
        for (int i = pageNum*10+1; i <=pageNum * 10 + rowNum; i++) {
            if (resultSet.next()) {
                product_names += i + ") " + resultSet.getString(2) + "\n";
                if (i <= pageNum*10+5) {
                    InlineKeyboardButton button = new InlineKeyboardButton()
                            .setText(String.valueOf(i))
                            .setCallbackData("productId_" + resultSet.getInt(1));
                    rowFirst.add(button);
                } else if (i >= pageNum*10+6) {
                    InlineKeyboardButton button1 = new InlineKeyboardButton()
                            .setText(String.valueOf(i))
                            .setCallbackData("productId_" + resultSet.getInt(1));
                    rowSecond.add(button1);
                }
            }
        }
        InlineKeyboardButton back = new InlineKeyboardButton()
                .setText(lang.equals("UZ")?"Orqaga\uD83D\uDD19":"Назад\uD83D\uDD19")
                .setCallbackData("/back");
        InlineKeyboardButton next = new InlineKeyboardButton()
                .setText(lang.equals("UZ")?"Oldinga\uD83D\uDD1C":"Далее\uD83D\uDD1C")
                .setCallbackData("/next");
        rowThird.add(back);
        rowThird.add(next);
        List<InlineKeyboardButton> rowFourth = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Savatcha\uD83D\uDED2" : "Карзина\uD83D\uDED2")
                .setCallbackData("showBasket");
        rowFourth.add(button);
        List<InlineKeyboardButton> rowFifth = new ArrayList<>();
        InlineKeyboardButton mainMenu = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Bosh menyu◀️" : "Главный меню◀️")
                .setCallbackData("backToMain");
        rowFifth.add(mainMenu);
        buttons.add(rowFirst);
        buttons.add(rowSecond);
        buttons.add(rowThird);
        buttons.add(rowFourth);
        buttons.add(rowFifth);
        markup.setKeyboard(buttons);
        message.setText(product_names);
        message.setReplyMarkup(markup);
        String sql3 = "UPDATE users SET page_num=? WHERE tg_user_id=?";
        PreparedStatement ps3 = connection.prepareStatement(sql3);
        ps3.setInt(1, pageNum+1);
        ps3.setLong(2, user.getTgUserId());
        ps3.executeUpdate();
        ps3.close();
        connection.close();
        return message;
    }

    @SneakyThrows
    public static SendMessage backPage(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        message.setChatId(chatId);
        User user = hasUser(update);
        int pageNum = user.getPageNum()-2;
        String lang = user.getLanguage().name();
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(TgBotServis.url);
        String sql = "select id, name_uz from product limit 10 offset "+(pageNum * 10);
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(sql);
        resultSet.last();
        int rowNum = resultSet.getRow();
        resultSet.beforeFirst();
        String product_names = "";

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> rowFirst = new ArrayList<>();
        List<InlineKeyboardButton> rowSecond = new ArrayList<>();
        List<InlineKeyboardButton> rowThird = new ArrayList<>();
        for (int i = pageNum*10+1; i <=pageNum * 10 + rowNum; i++) {
            if (resultSet.next()) {
                product_names += i + ") " + resultSet.getString(2) + "\n";
                if (i <= pageNum*10+5) {
                    InlineKeyboardButton button = new InlineKeyboardButton()
                            .setText(String.valueOf(i))
                            .setCallbackData("productId_" + resultSet.getInt(1));
                    rowFirst.add(button);
                } else if (i >= pageNum*10+6) {
                    InlineKeyboardButton button1 = new InlineKeyboardButton()
                            .setText(String.valueOf(i))
                            .setCallbackData("productId_" + resultSet.getInt(1));
                    rowSecond.add(button1);
                }
            }
        }
        InlineKeyboardButton back = new InlineKeyboardButton()
                .setText(lang.equals("UZ")?"Orqaga\uD83D\uDD19":"Назад\uD83D\uDD19")
                .setCallbackData("/back");
        InlineKeyboardButton next = new InlineKeyboardButton()
                .setText(lang.equals("UZ")?"Oldinga\uD83D\uDD1C":"Далее\uD83D\uDD1C")
                .setCallbackData("/next");
        rowThird.add(back);
        rowThird.add(next);
        List<InlineKeyboardButton> rowFourth = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Savatcha\uD83D\uDED2" : "Карзина\uD83D\uDED2")
                .setCallbackData("showBasket");
        rowFourth.add(button);
        List<InlineKeyboardButton> rowFifth = new ArrayList<>();
        InlineKeyboardButton mainMenu = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Bosh menyu◀️" : "Главный меню◀️")
                .setCallbackData("backToMain");
        rowFifth.add(mainMenu);
        buttons.add(rowFirst);
        buttons.add(rowSecond);
        buttons.add(rowThird);
        buttons.add(rowFourth);
        buttons.add(rowFifth);
        markup.setKeyboard(buttons);
        message.setText(product_names);
        message.setReplyMarkup(markup);
            String sql1 = "UPDATE users SET page_num=? WHERE tg_user_id=?";
            PreparedStatement ps2 = connection.prepareStatement(sql1);
            ps2.setInt(1, user.getPageNum()-1);
            ps2.setLong(2, user.getTgUserId());
            ps2.executeUpdate();
            ps2.close();
        connection.close();
        return message;
    }

    @SneakyThrows
    public static SendPhoto showProduct(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String productId = update.getCallbackQuery().getData().replace("productId_", "");
        SendPhoto sendPhoto = new SendPhoto();
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(TgBotServis.url);

        String sql1 = "UPDATE users SET product_id=?, amount=? WHERE tg_user_id=?";
        PreparedStatement ps = connection.prepareStatement(sql1);
        ps.setInt(1, Integer.parseInt(productId));
        ps.setInt(2, 1);
        ps.setLong(3, chatId);
        ps.executeUpdate();
        ps.close();

        User user = hasUser(update);
        changeBotState(user,BotState.SHOW_PRODUCT);
        String lang = user.getLanguage().name();

        String sql = "select name_uz, description_uz, description_ru, sale_price, bytes from product " +
                "join attachment_content using(attachment_id) where product.id=" + productId;
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        Product product = new Product();
        AttachmentContent content = new AttachmentContent();
        if (resultSet.next()) {
            product.setNameUz(resultSet.getString(1));
            product.setDescriptionUz(resultSet.getString(2));
            product.setDescriptionRu(resultSet.getString(3));
            product.setSalePrice(resultSet.getDouble(4));
            content.setBytes(resultSet.getBytes(5));
        }
        sendPhoto.setCaption(product.getNameUz() + "\n\n" + (lang.equals("UZ") ? product.getDescriptionUz() + "\n\n Narxi: " : product.getDescriptionRu() + "\n\n Цена: ") + product.getSalePrice());
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        sendPhoto.setPhoto("product", inputStream);
        statement.close();
        resultSet.close();
        connection.close();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> button = new ArrayList<>();
        List<InlineKeyboardButton> rowFirst = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton()
                .setText(" ➖ ")
                .setCallbackData("minus");
        InlineKeyboardButton button2 = new InlineKeyboardButton()
                .setText(String.valueOf(user.getAmount()))
                .setCallbackData("amount");
        InlineKeyboardButton button3 = new InlineKeyboardButton()
                .setText(" ➕ ")
                .setCallbackData("plus");
        rowFirst.add(button1);
        rowFirst.add(button2);
        rowFirst.add(button3);

        List<InlineKeyboardButton> rowSecond = new ArrayList<>();
        InlineKeyboardButton button6 = new InlineKeyboardButton()
                .setText(lang.equals("UZ")?" Savatga qo'shish\uD83D\uDCE5 ":" Добавить в корзину\uD83D\uDCE5")
                .setCallbackData("/addToBasket");
        rowSecond.add(button6);
        List<InlineKeyboardButton> rowThird = new ArrayList<>();
        InlineKeyboardButton showBasket = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Savatcha\uD83D\uDED2" : "Карзина\uD83D\uDED2")
                .setCallbackData("showBasket");
        rowThird.add(showBasket);


        List<InlineKeyboardButton> rowFourth = new ArrayList<>();
        InlineKeyboardButton button4 = new InlineKeyboardButton()
                .setText(lang.equals("UZ")?" Orqaga\uD83D\uDD19 ":" Назад \uD83D\uDD19")
                .setCallbackData("startOrder");
        InlineKeyboardButton mainMenu = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? " Bosh menyu◀️ " : " Главный меню◀️ ")
                .setCallbackData("backToMain");
        rowFourth.add(button4);
        rowFourth.add(mainMenu);
        button.add(rowFirst);
        button.add(rowSecond);
        button.add(rowThird);
        button.add(rowFourth);
        markup.setKeyboard(button);
        sendPhoto.setReplyMarkup(markup);
        sendPhoto.setChatId(chatId);
        return sendPhoto;
    }

    @SneakyThrows
    public static AnswerCallbackQuery amount(Update update) {
        AnswerCallbackQuery query = new AnswerCallbackQuery();
        query.setCallbackQueryId(update.getCallbackQuery().getId());
        User user = hasUser(update);
        String lang = user.getLanguage().name();
        query.setShowAlert(false);
        query.setText(lang.equals("UZ")?"Siz "+user.getAmount()+" ta mahsulot buyurtma qilmoqdasiz!":"Вы заказываете "+user.getAmount()+" товаров!");
        return query;
    }


    @SneakyThrows
    public static AnswerCallbackQuery firstPage(Update update) {
        AnswerCallbackQuery query = new AnswerCallbackQuery();
        query.setCallbackQueryId(update.getCallbackQuery().getId());
        User user = hasUser(update);
        String lang = user.getLanguage().name();
        query.setShowAlert(true);
        query.setText(lang.equals("UZ")?"Siz birinchi sahifadasiz":"Вы на первой странице");
        return query;
    }

    @SneakyThrows
    public static AnswerCallbackQuery lastPage(Update update) {
        AnswerCallbackQuery query = new AnswerCallbackQuery();
        query.setCallbackQueryId(update.getCallbackQuery().getId());
        User user = hasUser(update);
        String lang = user.getLanguage().name();
        query.setShowAlert(true);
        query.setText(lang.equals("UZ")?"Siz oxirgi sahifadasiz":"Вы на последней странице");
        return query;
    }

    @SneakyThrows
    public static void setLastMessageId(Message message) {
        Integer messageId = message.getMessageId();
        Long chatId = message.getChatId();
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(TgBotServis.url);
        String sql1 = "UPDATE users SET old_message_id=? WHERE tg_user_id=?";
        PreparedStatement ps = connection.prepareStatement(sql1);
        ps.setInt(1, messageId);
        ps.setLong(2, chatId);
        ps.executeUpdate();
        ps.close();
        connection.close();
    }

    public static void addProduct() throws ClassNotFoundException, SQLException, IOException, FileNotFoundException {
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager.getConnection(TgBotServis.url);
        for (int i = 11; i <= 23 ; i++) {
            String path = "D:\\Bootcamp\\Database\\TG_BOT\\TG_G10_bot\\src\\main\\resources\\phone" + i + ".jpg";
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
            ps2.setString(1, "Telefon"+i);
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

    @SneakyThrows
    public static EditMessageReplyMarkup editAmount(Update update) {
        String edit = update.getCallbackQuery().getData();
        EditMessageReplyMarkup replyMarkup = new EditMessageReplyMarkup();
        User user = hasUser(update);
        String lang = user.getLanguage().name();
        replyMarkup.setMessageId(user.getOldMessageId());
        replyMarkup.setChatId(user.getTgUserId());
        changeBotState(user, BotState.GET_AMOUNT);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> button = new ArrayList<>();
        List<InlineKeyboardButton> rowFirst = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton()
                .setText(" ➖ ")
                .setCallbackData("minus");
        InlineKeyboardButton button2 = new InlineKeyboardButton()
                .setText(edit.equals("minus")?String.valueOf(user.getAmount()-1):String.valueOf(user.getAmount()+1))
                .setCallbackData("amount");
        InlineKeyboardButton button3 = new InlineKeyboardButton()
                .setText(" ➕ ")
                .setCallbackData("plus");
        rowFirst.add(button1);
        rowFirst.add(button2);
        rowFirst.add(button3);


        List<InlineKeyboardButton> rowSecond = new ArrayList<>();
        InlineKeyboardButton button6 = new InlineKeyboardButton()
                .setText(lang.equals("UZ")?" Savatga qo'shish\uD83D\uDCE5 ":" Добавить в корзину\uD83D\uDCE5")
                .setCallbackData("/addToBasket");
        rowSecond.add(button6);
        List<InlineKeyboardButton> rowThird = new ArrayList<>();
        InlineKeyboardButton showBasket = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Savatcha\uD83D\uDED2" : "Карзина\uD83D\uDED2")
                .setCallbackData("showBasket");
        rowThird.add(showBasket);


        List<InlineKeyboardButton> rowFourth = new ArrayList<>();
        InlineKeyboardButton button4 = new InlineKeyboardButton()
                .setText(lang.equals("UZ")?" Orqaga\uD83D\uDD19 ":" Назад \uD83D\uDD19")
                .setCallbackData("startOrder");
        InlineKeyboardButton mainMenu = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? " Bosh menyu◀️ " : " Главный меню◀️ ")
                .setCallbackData("backToMain");
        rowFourth.add(button4);
        rowFourth.add(mainMenu);
        button.add(rowFirst);
        button.add(rowSecond);
        button.add(rowThird);
        button.add(rowFourth);
        markup.setKeyboard(button);
        replyMarkup.setReplyMarkup(markup);

        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(TgBotServis.url);
        String sql1 = "UPDATE users SET amount=? WHERE tg_user_id=?";
        PreparedStatement ps = connection.prepareStatement(sql1);
        ps.setInt(1, edit.equals("minus")?user.getAmount()-1:user.getAmount()+1);
        ps.setLong(2, user.getTgUserId());
        ps.executeUpdate();
        ps.close();
        connection.close();
        return replyMarkup;
    }


    @SneakyThrows
    public static SendMessage addToBasket(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        User user = hasUser(update);
        Integer productId = user.getProductId();
        Integer amount = user.getAmount();
        String lang = user.getLanguage().name();
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager.getConnection(TgBotServis.url);
        String sql = "select sale_price from product where product.id=" + productId;
        Statement statement = c.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        Basket basket = new Basket();
        basket.setUserId(user.getId());
        basket.setProductId(productId);
        basket.setAmount(amount);
        if(resultSet.next()) {
            basket.setTotalSum(resultSet.getDouble(1)*amount);
        }
        statement.close();
        resultSet.close();
        PreparedStatement ps = c.prepareStatement("INSERT INTO basket(user_id, product_id, amount, total_sum) VALUES (?, ?, ?, ?)");
        ps.setInt(1, basket.getUserId());
        ps.setInt(2, basket.getProductId());
        ps.setInt(3, basket.getAmount());
        ps.setDouble(4, basket.getTotalSum());
        ps.executeUpdate();
        ps.close();



        message.setText(lang.equals("UZ")?" Savatga joylandi \uD83D\uDED2":" Добавлено в корзину \uD83D\uDED2");
        message.setParseMode(ParseMode.MARKDOWN);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> button = new ArrayList<>();
        List<InlineKeyboardButton> rowFirst = new ArrayList<>();
        InlineKeyboardButton showBasket = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "Savatcha\uD83D\uDED2" : "Карзина\uD83D\uDED2")
                .setCallbackData("showBasket");
        rowFirst.add(showBasket);
        List<InlineKeyboardButton> rowSecond = new ArrayList<>();
        InlineKeyboardButton button4 = new InlineKeyboardButton()
                .setText(lang.equals("UZ")?" Orqaga\uD83D\uDD19 ":" Назад \uD83D\uDD19")
                .setCallbackData("startOrder");
        InlineKeyboardButton mainMenu = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? " Bosh menyu◀️ " : " Главный меню◀️ ")
                .setCallbackData("backToMain");
        rowSecond.add(button4);
        rowSecond.add(mainMenu);
        button.add(rowFirst);
        button.add(rowSecond);
        markup.setKeyboard(button);
        message.setReplyMarkup(markup);
        message.setChatId(chatId);
        return message;
    }


    @SneakyThrows
    public static SendMessage showBasket(Update update){
        User user = hasUser(update);
        String lang = user.getLanguage().name();
        SendMessage message = new SendMessage();
        message.setParseMode(ParseMode.MARKDOWN);
        message.setChatId(user.getTgUserId());
        String messageString = "";
        double totalSum = 0;
        changeBotState(user, BotState.SHOW_BASKET);
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager.getConnection(TgBotServis.url);
        String sql = "select p.name_uz, b.amount, b.total_sum from basket b join product p on p.id=b.product_id where b.user_id="+user.getId();
        Statement statement = c.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        int numProduct = 0;
        while (resultSet.next()) {
            numProduct++;
            messageString+= lang.equals("UZ")?numProduct + ") *"+resultSet.getString(1)  + "  "+
                    resultSet.getInt(2) +" dona.  Jami summasi: " + resultSet.getDouble(3) + "*\n" :
                    numProduct + ") *"+resultSet.getString(1)  + "  "+
                            resultSet.getInt(2) +" штук.  Общая сумма: " + resultSet.getDouble(3) + "*\n" ;
            totalSum+=resultSet.getDouble(3);
        }
        if(numProduct==0) {
            message.setText(lang.equals("UZ")?"*Savatinggiz bo'sh*\uD83D\uDEAB":"*Ваша корзина пуста*\uD83D\uDEAB");
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> button = new ArrayList<>();
            List<InlineKeyboardButton> rowFirst = new ArrayList<>();
            InlineKeyboardButton startOrder = new InlineKeyboardButton()
                    .setText(lang.equals("UZ") ? "Mahsulotlar\uD83D\uDCF1" : "Продукты\uD83D\uDCF1")
                    .setCallbackData("startOrder");
            InlineKeyboardButton mainMenu = new InlineKeyboardButton()
                    .setText(lang.equals("UZ") ? " Bosh menyu◀️ " : " Главный меню◀️ ")
                    .setCallbackData("backToMain");
            rowFirst.add(startOrder);
            rowFirst.add(mainMenu);
            button.add(rowFirst);
            markup.setKeyboard(button);
            message.setReplyMarkup(markup);
        } else {
            messageString+=(lang.equals("UZ")? "Barcha mahsulotlar uchun umumiy summa\uD83E\uDDFE: *":"Общая сумма по всем товарам\uD83E\uDDFE: *") + totalSum + "*";
            message.setText(messageString);
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> button = new ArrayList<>();
            List<InlineKeyboardButton> rowFirst = new ArrayList<>();
            InlineKeyboardButton confirmOrder = new InlineKeyboardButton()
                    .setText(lang.equals("UZ") ? "Buyurtmani tasdiqlash✅" : "Подтвердите заказ✅")
                    .setCallbackData("confirmOrderLocation");
            InlineKeyboardButton clearBasket = new InlineKeyboardButton()
                    .setText(lang.equals("UZ") ? "Savatni bo'shatish\uD83D\uDEAB" : "Очистить корзину\uD83D\uDEAB")
                    .setCallbackData("clearBasket");
            rowFirst.add(confirmOrder);
            rowFirst.add(clearBasket);
            List<InlineKeyboardButton> rowSecond = new ArrayList<>();
            InlineKeyboardButton startOrder = new InlineKeyboardButton()
                    .setText(lang.equals("UZ") ? "Mahsulotlar\uD83D\uDCF1" : "Продукты\uD83D\uDCF1")
                    .setCallbackData("startOrder");
            InlineKeyboardButton mainMenu = new InlineKeyboardButton()
                    .setText(lang.equals("UZ") ? " Bosh menyu◀️ " : " Главный меню◀️ ")
                    .setCallbackData("backToMain");
            rowSecond.add(startOrder);
            rowSecond.add(mainMenu);
            button.add(rowFirst);
            button.add(rowSecond);
            markup.setKeyboard(button);
            message.setReplyMarkup(markup);
        }
        statement.close();
        resultSet.close();
        return message;
    }

    @SneakyThrows
    public static void clearBasket(Update update) {
        User user = hasUser(update);
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager.getConnection(TgBotServis.url);
        PreparedStatement ps = c.prepareStatement("DELETE FROM basket WHERE user_id=?");
        ps.setInt(1, user.getId());
        ps.executeUpdate();
        ps.close();
    }


    @SneakyThrows
    public static SendMessage confirmOrderLocation(Update update) {
        User user = hasUser(update);
        changeBotState(user, BotState.SHARE_LOCATION);
        String lang = user.getLanguage().name();
        SendMessage message = new SendMessage();
        message.setParseMode(ParseMode.MARKDOWN);
        message.setChatId(user.getTgUserId());
        message.setText(lang.equals("UZ")?"Mahsulotlar yetkazilishi kerak bo'lgan *manzilinggizni bo'lishing yoki yozib yuboring*\uD83D\uDC47\uD83C\uDFFB":
                "*Поделитесь или запишите адрес*, по которому должны быть доставлены товары\uD83D\uDC47\uD83C\uDFFB");
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setSelective(true);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);
        List<KeyboardRow> row = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton(lang.equals("UZ") ? "Lokatsiyani yuborish\uD83D\uDCCD" : "Отправить местоположение\uD83D\uDCCD").setRequestLocation(true));
        row.add(keyboardRow);
        keyboard.setKeyboard(row);
        message.setParseMode(ParseMode.MARKDOWN);
        message.setReplyMarkup(keyboard);
        return message;
    }

    @SneakyThrows
    public static SendMessage askConfirmOrder(Update update) {
        SendMessage message = new SendMessage();
        User user = hasUser(update);
        String lang = user.getLanguage().name();
        message.setParseMode(ParseMode.MARKDOWN);
        message.setChatId(user.getTgUserId());
        changeBotState(user, BotState.CONFIRM_ORDER);
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager.getConnection(TgBotServis.url);
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(calendar.getTimeInMillis());
        Order order = new Order();
        order.setCreatedAt(date);
        order.setStatus(OrderStatus.DRAFT);
        order.setUser(user);
        if(update.getMessage().getLocation()!=null) {
            Float longitude = update.getMessage().getLocation().getLongitude();
            Float latitude = update.getMessage().getLocation().getLatitude();
            order.setLon(longitude);
            order.setLat(latitude);
            PreparedStatement ps = c.prepareStatement("INSERT INTO orders(created_at, status, user_id, lon, lat) VALUES (?, ?, ?, ?, ?)");
            ps.setDate(1,order.getCreatedAt());
            ps.setString(2,String.valueOf(order.getStatus()));
            ps.setInt(3, order.getUser().getId());
            ps.setFloat(4,order.getLon());
            ps.setFloat(5,order.getLat());
            ps.executeUpdate();
            ps.close();
        } else {
            String address = update.getMessage().getText();
            order.setAddress(address);
            PreparedStatement ps = c.prepareStatement("INSERT INTO orders(created_at, status, user_id, address) VALUES (?, ?, ?, ?)");
            ps.setDate(1,order.getCreatedAt());
            ps.setString(2,String.valueOf(order.getStatus()));
            ps.setInt(3, order.getUser().getId());
            ps.setString(4,order.getAddress());
            ps.executeUpdate();
            ps.close();
        }
        message.setText(lang.equals("UZ")?"*Buyurtmani tasdiqlaysizmi?*":"*Вы подтверждаете заказ?*");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> button = new ArrayList<>();
        List<InlineKeyboardButton> rowFirst = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "  Ha ✅  " : "  Да ✅  ")
                .setCallbackData("confirmOrder");
        InlineKeyboardButton button2 = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? "  Yo'q ❌  " : "  Нет ❌  ")
                .setCallbackData("cancelOrder");
        rowFirst.add(button1);
        rowFirst.add(button2);
        button.add(rowFirst);
        markup.setKeyboard(button);
        message.setReplyMarkup(markup);
        return message;
    }

    @SneakyThrows
    public static SendMessage confirmOrder(Update update) {
        SendMessage message = new SendMessage();
        User user = hasUser(update);
        changeBotState(user, BotState.FINISH_ORDER);
        String lang = user.getLanguage().name();
        message.setChatId(user.getTgUserId());
        message.setParseMode(ParseMode.MARKDOWN);
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager.getConnection(TgBotServis.url);
        String sql = "select product_id, amount, total_sum from basket where user_id="+user.getId();
        Statement statement = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(sql);
        double totalSum = 0;
        while (resultSet.next()) {
            totalSum+=resultSet.getDouble(3);
        }
        resultSet.beforeFirst();
        int orderId = getOrderId(user);
        while(resultSet.next()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO order_product(product_id, amount, order_id) VALUES (?, ?, ?)");
            ps.setInt(1,resultSet.getInt(1));
            ps.setInt(2,resultSet.getInt(2));
            ps.setInt(3, orderId);
            ps.executeUpdate();
            ps.close();
        }
        statement.close();
        resultSet.close();
        String sql1 = "UPDATE orders SET status=?, total_sum=? WHERE id=?";
        PreparedStatement ps = c.prepareStatement(sql1);
        ps.setString(1, String.valueOf(OrderStatus.NEW));
        ps.setDouble(2, totalSum);
        ps.setInt(3, orderId);
        ps.executeUpdate();
        ps.close();
        PreparedStatement ps1 = c.prepareStatement("DELETE FROM basket WHERE user_id=?");
        ps1.setInt(1, user.getId());
        ps1.executeUpdate();
        ps1.close();
        c.close();

        message.setText(lang.equals("UZ")?"*Xaridinggiz uchun rahmat! Tez orada operatorlarimiz siz bilan bog'lanishadi.*":"*Спасибо за покупку! Наши операторы свяжутся с вами в ближайшее время.*");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> button = new ArrayList<>();
        List<InlineKeyboardButton> rowFirst = new ArrayList<>();
        InlineKeyboardButton mainMenu = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? " Bosh menyu◀️ " : " Главный меню◀️ ")
                .setCallbackData("backToMain");
        rowFirst.add(mainMenu);
        button.add(rowFirst);
        markup.setKeyboard(button);
        message.setReplyMarkup(markup);
        return message;
    }

    @SneakyThrows
    public static SendMessage cancelOrder(Update update) {
        SendMessage message  = new SendMessage();
        User user = hasUser(update);
        changeBotState(user, BotState.FINISH_ORDER);
        String lang = user.getLanguage().name();
        message.setChatId(user.getTgUserId());
        message.setParseMode(ParseMode.MARKDOWN);
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager.getConnection(TgBotServis.url);
        int orderId = getOrderId(user);
        String sql1 = "UPDATE orders SET status=? WHERE id=?";
        PreparedStatement ps = c.prepareStatement(sql1);
        ps.setString(1, String.valueOf(OrderStatus.CANCELED));
        ps.setInt(2, orderId);
        ps.executeUpdate();
        ps.close();
        message.setText(lang.equals("UZ")?"*Xaridinggiz bekor qilindi!*":"Ваша покупка отменена!");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> button = new ArrayList<>();
        List<InlineKeyboardButton> rowFirst = new ArrayList<>();
        InlineKeyboardButton mainMenu = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? " Bosh menyu◀️ " : " Главный меню◀️ ")
                .setCallbackData("backToMain");
        rowFirst.add(mainMenu);
        button.add(rowFirst);
        markup.setKeyboard(button);
        message.setReplyMarkup(markup);
        return message;
    }


    @SneakyThrows
    public static int getOrderId(User user) {
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager.getConnection(TgBotServis.url);
        String sql = "select id from orders where user_id="+user.getId()+" and status="+"'DRAFT'";
        Statement statement = c.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        if(resultSet.next()) {
            return resultSet.getInt(1);
        }
        return 0;
    }

    @SneakyThrows
    public static SendMessage showOrders(Update update) {
        SendMessage message = new SendMessage();
        User user = hasUser(update);
        String lang = user.getLanguage().name();
        message.setChatId(user.getTgUserId());
        message.setParseMode(ParseMode.MARKDOWN);
        changeBotState(user, BotState.SHOW_ORDERS);
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(TgBotServis.url);
        String sql3 = "UPDATE users SET page_num=? WHERE tg_user_id=?";
        PreparedStatement ps3 = connection.prepareStatement(sql3);
        ps3.setInt(1, 1);
        ps3.setLong(2, user.getTgUserId());
        ps3.executeUpdate();
        ps3.close();
        int lastPage;
        String sql1 = "select count(id) from orders where user_id="+user.getId() + " and status not in('DRAFT', 'CANCELED')";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql1);
        boolean isHas = false;
        if(rs.next() && rs.getInt(1)!=0) {
            lastPage = rs.getInt(1);
            st.close();
            rs.close();
            String sql2 = "UPDATE users SET last_page_num=? WHERE tg_user_id=?";
            PreparedStatement ps2 = connection.prepareStatement(sql2);
            ps2.setInt(1, lastPage);
            ps2.setLong(2, user.getTgUserId());
            ps2.executeUpdate();
            ps2.close();
            String sql4 = "select id from orders where user_id="+user.getId()+" and status not in('DRAFT', 'CANCELED') order by id desc limit 1";
            Statement st4 = connection.createStatement();
            ResultSet rs4 = st4.executeQuery(sql4);
            int orderId = 0;
            if(rs4.next()) {
               orderId = rs4.getInt(1);
            }
            st4.close();
            rs4.close();
            String sql = "SELECT P.NAME_UZ, OP.AMOUNT, P.SALE_PRICE, TO_CHAR(CREATED_AT, 'DD.MM.YYYY') AS DATE, O.STATUS, O.TOTAL_SUM FROM ORDER_PRODUCT OP " +
                    "       JOIN PRODUCT P ON P.ID = OP.PRODUCT_ID JOIN ORDERS O ON O.ID = OP.ORDER_ID " +
                    "       where o.id="+orderId;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            int numProduct = 0;
            double totalSum = 0;
            String orderStatus="";
            String date = "";
            String messageString = "";
            while (resultSet.next()) {
                numProduct++;
                messageString+= lang.equals("UZ")?numProduct + ") *"+resultSet.getString(1)  + "  "+
                        resultSet.getInt(2) +" dona.  1 donasining narxi: " + resultSet.getDouble(3) + "*\n" :
                        numProduct + ") *"+resultSet.getString(1)  + "  "+
                                resultSet.getInt(2) +" штук.  Цена 1 шт.: " + resultSet.getDouble(3) + "*\n";
                date = resultSet.getString(4);
                orderStatus = orderStatus(resultSet.getString(5), lang);
                totalSum = resultSet.getDouble(6);
            }
            messageString+= lang.equals("UZ")?"Umumiy summasi: *"+totalSum+"*\nBuyurtma xolati: *"+orderStatus+"*\nBuyurtma qilingan sana: *"+date+"*":
                    "Общая сумма: *"+totalSum+"*\nСтатус заказа: *"+orderStatus+"*\nДата заказа: *"+date+"*";
            message.setText(messageString);
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> button = new ArrayList<>();
            List<InlineKeyboardButton> rowFirst = new ArrayList<>();
            List<InlineKeyboardButton> rowSecond = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton()
                    .setText(" \uD83D\uDD19 ")
                    .setCallbackData("/backOrder");
            InlineKeyboardButton button2 = new InlineKeyboardButton()
                    .setText(1 +"/"+lastPage)
                    .setCallbackData("/numPage");
            InlineKeyboardButton button3 = new InlineKeyboardButton()
                    .setText(" \uD83D\uDD1C ")
                    .setCallbackData("/nextOrder");
            rowFirst.add(button1);
            rowFirst.add(button2);
            rowFirst.add(button3);
            InlineKeyboardButton mainMenu = new InlineKeyboardButton()
                    .setText(lang.equals("UZ") ? " Bosh menyu◀️ " : " Главный меню◀️ ")
                    .setCallbackData("backToMain");
            rowSecond.add(mainMenu);
            button.add(rowFirst);
            button.add(rowSecond);
            markup.setKeyboard(button);
            message.setReplyMarkup(markup);
        } else {
            message.setText(lang.equals("UZ")?"*Sizda buyurtmalar yo'q!*\uD83D\uDEAB":"*У вас нет заказов!*\uD83D\uDEAB");
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> button = new ArrayList<>();
            List<InlineKeyboardButton> rowFirst = new ArrayList<>();
            InlineKeyboardButton mainMenu = new InlineKeyboardButton()
                    .setText(lang.equals("UZ") ? " Bosh menyu◀️ " : " Главный меню◀️ ")
                    .setCallbackData("backToMain");
            rowFirst.add(mainMenu);
            button.add(rowFirst);
            markup.setKeyboard(button);
            message.setReplyMarkup(markup);
        }
        return message;
    }

    @SneakyThrows
    public static SendMessage nextOrder(Update update) {
        SendMessage message = new SendMessage();
        User user = hasUser(update);
        message.setChatId(user.getTgUserId());
        message.setParseMode(ParseMode.MARKDOWN);
        int pageNum = user.getPageNum();
        String lang = user.getLanguage().name();
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(TgBotServis.url);

        String sql4 = "select id from orders where user_id="+user.getId()+" and status not in('DRAFT', 'CANCELED') order by id desc limit 1 offset "+pageNum;
        Statement st4 = connection.createStatement();
        ResultSet rs4 = st4.executeQuery(sql4);
        int orderId = 0;
        if(rs4.next()) {
            orderId = rs4.getInt(1);
        }
        st4.close();
        rs4.close();
        String sql = "SELECT P.NAME_UZ, OP.AMOUNT, P.SALE_PRICE, TO_CHAR(CREATED_AT, 'DD.MM.YYYY') AS DATE, O.STATUS, O.TOTAL_SUM FROM ORDER_PRODUCT OP " +
                "       JOIN PRODUCT P ON P.ID = OP.PRODUCT_ID JOIN ORDERS O ON O.ID = OP.ORDER_ID " +
                "       where o.id="+orderId;
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        int numProduct = 0;
        double totalSum = 0;
        String orderStatus="";
        String date = "";
        String messageString = "";
        while (resultSet.next()) {
            numProduct++;
            messageString+= lang.equals("UZ")?numProduct + ") *"+resultSet.getString(1)  + "  "+
                    resultSet.getInt(2) +" dona.  1 donasining narxi: " + resultSet.getDouble(3) + "*\n" :
                    numProduct + ") *"+resultSet.getString(1)  + "  "+
                            resultSet.getInt(2) +" штук.  Цена 1 шт.: " + resultSet.getDouble(3) + "*\n";
            date = resultSet.getString(4);
            orderStatus = orderStatus(resultSet.getString(5), lang);
            totalSum = resultSet.getDouble(6);
        }
        messageString+= lang.equals("UZ")?"Umumiy summasi: *"+totalSum+"*\nBuyurtma xolati: *"+orderStatus+"*\nBuyurtma qilingan sana: *"+date+"*":
                "Общая сумма: *"+totalSum+"*\nСтатус заказа: *"+orderStatus+"*\nДата заказа: *"+date+"*";
        message.setText(messageString);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> button = new ArrayList<>();
        List<InlineKeyboardButton> rowFirst = new ArrayList<>();
        List<InlineKeyboardButton> rowSecond = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton()
                .setText(" \uD83D\uDD19 ")
                .setCallbackData("/backOrder");
        InlineKeyboardButton button2 = new InlineKeyboardButton()
                .setText((pageNum+1) +"/"+user.getLastPageNum())
                .setCallbackData("/numPage");
        InlineKeyboardButton button3 = new InlineKeyboardButton()
                .setText(" \uD83D\uDD1C ")
                .setCallbackData("/nextOrder");
        rowFirst.add(button1);
        rowFirst.add(button2);
        rowFirst.add(button3);
        InlineKeyboardButton mainMenu = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? " Bosh menyu◀️ " : " Главный меню◀️ ")
                .setCallbackData("backToMain");
        rowSecond.add(mainMenu);
        button.add(rowFirst);
        button.add(rowSecond);
        markup.setKeyboard(button);
        message.setReplyMarkup(markup);
        String sql3 = "UPDATE users SET page_num=? WHERE tg_user_id=?";
        PreparedStatement ps3 = connection.prepareStatement(sql3);
        ps3.setInt(1, pageNum+1);
        ps3.setLong(2, user.getTgUserId());
        ps3.executeUpdate();
        ps3.close();
        connection.close();
        return message;
    }

    @SneakyThrows
    public static SendMessage backOrder(Update update) {
        SendMessage message = new SendMessage();
        User user = hasUser(update);
        message.setChatId(user.getTgUserId());
        int pageNum = user.getPageNum()-2;
        message.setParseMode(ParseMode.MARKDOWN);
        String lang = user.getLanguage().name();
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(TgBotServis.url);
        String sql4 = "select id from orders where user_id="+user.getId()+" and status not in('DRAFT', 'CANCELED') order by id desc limit 1 offset "+pageNum;
        Statement st4 = connection.createStatement();
        ResultSet rs4 = st4.executeQuery(sql4);
        int orderId = 0;
        if(rs4.next()) {
            orderId = rs4.getInt(1);
        }
        st4.close();
        rs4.close();
        String sql = "SELECT P.NAME_UZ, OP.AMOUNT, P.SALE_PRICE, TO_CHAR(CREATED_AT, 'DD.MM.YYYY') AS DATE, O.STATUS, O.TOTAL_SUM FROM ORDER_PRODUCT OP " +
                "       JOIN PRODUCT P ON P.ID = OP.PRODUCT_ID JOIN ORDERS O ON O.ID = OP.ORDER_ID " +
                "       where o.id="+orderId;
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        int numProduct = 0;
        double totalSum = 0;
        String orderStatus="";
        String date = "";
        String messageString = "";
        while (resultSet.next()) {
            numProduct++;
            messageString+= lang.equals("UZ")?numProduct + ") *"+resultSet.getString(1)  + "  "+
                    resultSet.getInt(2) +" dona.  1 donasining narxi: " + resultSet.getDouble(3) + "*\n" :
                    numProduct + ") *"+resultSet.getString(1)  + "  "+
                            resultSet.getInt(2) +" штук.  Цена 1 шт.: " + resultSet.getDouble(3) + "*\n";
            date = resultSet.getString(4);
            orderStatus = orderStatus(resultSet.getString(5), lang);
            totalSum = resultSet.getDouble(6);
        }
        messageString+= lang.equals("UZ")?"Umumiy summasi: *"+totalSum+"*\nBuyurtma xolati: *"+orderStatus+"*\nBuyurtma qilingan sana: *"+date+"*":
                "Общая сумма: *"+totalSum+"*\nСтатус заказа: *"+orderStatus+"*\nДата заказа: *"+date+"*";
        message.setText(messageString);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> button = new ArrayList<>();
        List<InlineKeyboardButton> rowFirst = new ArrayList<>();
        List<InlineKeyboardButton> rowSecond = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton()
                .setText(" \uD83D\uDD19 ")
                .setCallbackData("/backOrder");
        InlineKeyboardButton button2 = new InlineKeyboardButton()
                .setText((pageNum+1) +"/"+user.getLastPageNum())
                .setCallbackData("/numPage");
        InlineKeyboardButton button3 = new InlineKeyboardButton()
                .setText(" \uD83D\uDD1C ")
                .setCallbackData("/nextOrder");
        rowFirst.add(button1);
        rowFirst.add(button2);
        rowFirst.add(button3);
        InlineKeyboardButton mainMenu = new InlineKeyboardButton()
                .setText(lang.equals("UZ") ? " Bosh menyu◀️ " : " Главный меню◀️ ")
                .setCallbackData("backToMain");
        rowSecond.add(mainMenu);
        button.add(rowFirst);
        button.add(rowSecond);
        markup.setKeyboard(button);
        message.setReplyMarkup(markup);
        String sql1 = "UPDATE users SET page_num=? WHERE tg_user_id=?";
        PreparedStatement ps2 = connection.prepareStatement(sql1);
        ps2.setInt(1, user.getPageNum()-1);
        ps2.setLong(2, user.getTgUserId());
        ps2.executeUpdate();
        ps2.close();
        connection.close();
        return message;
    }

    public static String orderStatus(String s, String lang) {
        String orderStatus="";
        switch (s) {
            case "NEW":
                if(lang.equals("UZ")) {
                    orderStatus="Tekshirilmoqda";
                } else {
                    orderStatus="Проверка";
                }
            break;
            case "CONFIRMED":
                if(lang.equals("UZ")) {
                    orderStatus = "Tekshiruvdan o'tdi";
                } else {
                    orderStatus = "Прошел проверку";
                }
            break;
            case "SENT" :
                if(lang.equals("UZ")) {
                    orderStatus = "Jo'natilgan";
                } else {
                    orderStatus = "Отправленный";
                }
            break;
            case "RECEIVED" :
                if(lang.equals("UZ")) {
                    orderStatus = "Qabul qilingan";
                } else {
                    orderStatus = "Принял";
                }
            break;
            case "REJECTED" :
                if(lang.equals("UZ")) {
                    orderStatus = "Qabul qilinmagan";
                } else {
                    orderStatus = "Не принято";
                }
            break;
        }
        return orderStatus;
    }

    @SneakyThrows
    public static AnswerCallbackQuery numPage(Update update) {
        AnswerCallbackQuery query = new AnswerCallbackQuery();
        query.setCallbackQueryId(update.getCallbackQuery().getId());
        User user = hasUser(update);
        String lang = user.getLanguage().name();
        query.setShowAlert(false);
        query.setText(lang.equals("UZ")?"Siz "+user.getPageNum()+"-sahifadasiz.":"Вы находитесь на странице "+user.getPageNum()+".");
        return query;
    }

    @SneakyThrows
    public static SendMessage alertChangeLanguage(Update update) {
        SendMessage message = new SendMessage();
        User user = hasUser(update);
        message.setChatId(user.getTgUserId());
        message.setText(user.getLanguage().name().equals("UZ") ?
                "Til tanlang" : "Выберите язык");
        Map<String, String> map=new LinkedHashMap<>();
        map.put(user.getLanguage().name().equals("UZ") ?"UZ\uD83C\uDDFA\uD83C\uDDFF":"УЗ\uD83C\uDDFA\uD83C\uDDFF", "uz");
        map.put(user.getLanguage().name().equals("UZ") ?"RU\uD83C\uDDF7\uD83C\uDDFA":"РУ\uD83C\uDDF7\uD83C\uDDFA", "ru");
        message.setReplyMarkup( generateInline(map, 2));
        return message;
    }

    @SneakyThrows
    public static AnswerCallbackQuery alertAmount(Update update) {
        AnswerCallbackQuery query = new AnswerCallbackQuery();
        query.setCallbackQueryId(update.getCallbackQuery().getId());
        User user = hasUser(update);
        String lang = user.getLanguage().name();
        query.setShowAlert(false);
        query.setText(lang.equals("UZ")?"Eng kam miqdor tanlangan❗️":"Выбрано минимальное количество❗️");
        return query;
    }
}

