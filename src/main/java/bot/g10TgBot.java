package bot;

import lombok.SneakyThrows;
import model.User;
import model.enums.BotState;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

public class g10TgBot extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return "@phone_shop_uz_bot";
    }

    @Override
    public String getBotToken() {
        return "1766684680:AAH-Xt9-YbdmdGtgOQ_OXDo7pliXTtuI-as";
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()){
            if (update.getMessage().getContact()!=null){
                execute(TgBotServis.removeKeyboard(update));
                execute(TgBotServis.setContact(update));
            }
            else if (update.getMessage().getLocation()!=null){
                    execute(TgBotServis.removeKeyboard(update));
                    execute(TgBotServis.askConfirmOrder(update));
            }
            else {
                String text = update.getMessage().getText();
                User user = TgBotServis.hasUser(update);
                if (text.equals("/start")){
                    execute(TgBotServis.start(update));
                }else {
                    if (text.contains("Bosh") || text.contains("главный")){
                        execute(TgBotServis.removeKeyboardMainMenu(update));
                        execute(TgBotServis.backShowMainMenu(update));
                    }
                    if(user.getBotState().equals(BotState.CHANGE_LANG)) {
                        execute(TgBotServis.alertChangeLanguage(update));
                    }
                    if(user.getBotState().equals(BotState.CHOOSE_LANG)) {
                        execute(TgBotServis.start(update));
                    }
                    if (user.getBotState().equals(BotState.SHARE_CONTACT)){
                        execute(TgBotServis.removeKeyboard(update));
                        execute(TgBotServis.setContact(update));
                    }
                    else if (user.getBotState().equals(BotState.SHARE_NAME)){
                        execute(TgBotServis.updateName(update));
                    }
                    else if (user.getBotState().equals(BotState.CHANGE_NAME)
                    || user.getBotState().equals(BotState.CHANGE_PHONE)) {
                        execute(TgBotServis.getChange(update));
                    } else if(user.getBotState().equals(BotState.SHARE_LOCATION)) {
                        execute(TgBotServis.removeKeyboard(update));
                        execute(TgBotServis.askConfirmOrder(update));
                    }
                }
            }

        }else if (update.getCallbackQuery()!=null){
            String data = update.getCallbackQuery().getData();
            User user = TgBotServis.hasUser(update);
            if (data.equals("uz") || data.equals("ru")){
                if(user.getBotState().equals(BotState.CHANGE_LANG)) {
                    execute(TgBotServis.getChange(update));
                } else {
                    execute(TgBotServis.setLang(update));
                }
            }
            else if (data.equals("changeName")){
                    execute(TgBotServis.change(update,"name"));
            }
            else if (data.equals("changePhone")){
                execute(TgBotServis.change(update,"phone"));
            }
            else if (data.equals("changeLanguage")){
                execute(TgBotServis.change(update,"lang"));
            }
            else if (data.contains("Name")){
                execute(TgBotServis.setOrAskName(update));
            }
            else if(data.equals("aboutUs")){
                execute(TgBotServis.aboutUs(update));
            }
            else if (data.equals("settings")){
                execute(TgBotServis.showSettings(update));
            }else if(data.equals("backToMain")) {
                    execute(TgBotServis.backShowMainMenu(update));
            }
            else if(data.equals("startOrder")){
                execute(TgBotServis.startOrder(update));
            }
            else if(data.startsWith("productId")) {
                Message message = execute(TgBotServis.showProduct(update));
                TgBotServis.setLastMessageId(message);
            }  else if(data.equals("/next")) {
                if(user.getPageNum().equals(user.getLastPageNum())) {
                    execute(TgBotServis.lastPage(update));
                } else {
                    execute(TgBotServis.nextPage(update));
                }
            }
            else if(data.equals("/back")) {
                if(user.getPageNum()==1) {
                    execute(TgBotServis.firstPage(update));
                } else {
                    execute(TgBotServis.backPage(update));
                }
            } else if(data.equals("minus")||data.equals("plus")) {
                if(data.equals("minus")&&user.getAmount()==1) {
                    execute(TgBotServis.alertAmount(update));
                } else {
                    execute(TgBotServis.editAmount(update));
                }
            } else if (data.equals("/addToBasket")) {
                execute(TgBotServis.addToBasket(update));
            } else if(data.equals("showBasket")) {
                execute(TgBotServis.showBasket(update));
            }
            else if(data.equals("amount")) {
                execute(TgBotServis.amount(update));
            }
            else if(data.equals("clearBasket")) {
                TgBotServis.clearBasket(update);
                execute(TgBotServis.showBasket(update));
            }
            else if(data.equals("confirmOrderLocation")) {
                execute(TgBotServis.confirmOrderLocation(update));
            }
            else if(data.equals("confirmOrder")) {
                execute(TgBotServis.confirmOrder(update));
            }
            else if(data.equals("cancelOrder")) {
                execute(TgBotServis.cancelOrder(update));
            }
            else if(data.equals("showOrders")) {
                execute(TgBotServis.showOrders(update));
            }
            else if(data.equals("/nextOrder")) {
                if(user.getPageNum().equals(user.getLastPageNum())) {
                    execute(TgBotServis.lastPage(update));
                } else {
                    execute(TgBotServis.nextOrder(update));
                }
            }
            else if(data.equals("/backOrder")) {
                if(user.getPageNum()==1) {
                    execute(TgBotServis.firstPage(update));
                } else {
                    execute(TgBotServis.backOrder(update));
                }
            }
            else if(data.equals("/numPage")) {
                    execute(TgBotServis.numPage(update));
            }
        }
    }
}

