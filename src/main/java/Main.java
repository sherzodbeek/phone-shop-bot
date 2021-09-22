import bot.TgBotServis;
import bot.g10TgBot;
import lombok.SneakyThrows;
import org.telegram.telegrambots.ApiContextInitializer;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Main  {
    @SneakyThrows
    public static void main(String[] args) throws TelegramApiRequestException {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi=new TelegramBotsApi();
        telegramBotsApi.registerBot(new g10TgBot());
//        TgBotServis.addProduct();
    }

}
