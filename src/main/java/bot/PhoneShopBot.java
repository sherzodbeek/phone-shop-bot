package bot;

import lombok.SneakyThrows;
import model.User;
import model.enums.BotState;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.DeleteChatPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public class PhoneShopBot extends TelegramLongPollingBot {
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

            }
            else if (update.getMessage().getLocation()!=null){

            }
            else {
                String text = update.getMessage().getText();
                if (text.equals("/start")){
                    execute(BotService.start(update));
                }
            }

        }else if (update.getCallbackQuery()!=null){
            String text = update.getCallbackQuery().getData();
            User user = BotService.hasUser(update);
            if((text.equals("uz") || text.equals("ru")) && user.getBotState().equals(BotState.CHOOSE_LANG)){
                execute(BotService.setLang(update));
            }
        }
//        if(update.getMessage().getContact()!=null) {
////            Long chatId = update.getMessage().getChatId();
////            SendMessage sendMessage = new SendMessage();
////            sendMessage.setText("Salom");
////            sendMessage.setChatId(chatId);
////            Message message = execute(sendMessage);
////
////            Integer messageId = update.getMessage().getMessageId();
////            DeleteMessage deleteMessage = new DeleteMessage();
////            deleteMessage.setMessageId(messageId);
////            deleteMessage.setChatId(chatId);
////            execute(deleteMessage);
////
////            Integer messageId1 = message.getMessageId();
////            DeleteMessage deleteMessage1 = new DeleteMessage();
////            deleteMessage1.setMessageId(messageId1);
////            deleteMessage1.setChatId(chatId);
////            execute(deleteMessage1);
//        }
//        else if (update.getMessage().getLocation()!=null) {
//
//        } else if(update.getMessage().getText()!=null) {
//            if(update.getMessage().getText().equals("/start")) {
//
//            }
//        }
    }
}
