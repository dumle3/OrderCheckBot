/**********************************************************************************
 Начинаем работу - /start
 Выбираем цех и бот начинает присылать уведомления по вашему id в телеграм API (не ник)
 Завершаем работу - /stop - заказы не приходят

 id работников и их настоящий цех (или его отсутствие - значение "relax") хранятся в массиве team
 Каждые 10 секунд файл list.txt проыверяется на наличие изменений

 !!! Чтобы не было проблем со чтением заказов, рекомендую публикацию новых заказов осуществлять вдвое реже, чем чтение (20 и 10 сек соответственно)

 Мб: Список заказов очищается, когда все работники уходят отдыхать
***********************************************************************************/
package dimats;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Bot extends TelegramLongPollingBot {

    Map<Long, String> team = new HashMap<>();
    private static final Logger logger = Logger.getLogger("MyLogger");
    Timer timer = new Timer();
    Long hostId = 679672958L;
    List<Order> orders = new ArrayList<>();

    String ordersListTxt = "C://Users/dimcu/IdeaProjects/OrderCheckBot/src/list.txt";

    public Bot() {
        /*Следим за выходящими заказами, регулярно проверяя файл list.txt с помощью функции getOrders(fn);
         * и, при их наличии, помещаем новые в лист заказов orders, отправляем ожидающим работникам и удаляем из файла */
        TimerTask periodOrderCheck = new TimerTask() {
            public void run() {
                getOrders();
                for (Order order : orders) {
                    if (order.isPosted.equals(true))
                        continue;
                    switch (order.gild) {
                        case "all":
                        case "free":
                        case "cold":
                        case "hot":
                        case "pizza":
                        case "boss":
                            for (Long id : team.keySet()) {
                                if (order.gild.equals(team.get(id)) || team.get(id).equals("all")) {
                                    try {
                                        execute(sendOrder(order, hostId));
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                            order.isPosted = true;
                            break;
                        //"relax" gild exists only for the team, not for the orders
                        default:
                            sendText(hostId, "Order with incorrect gild type got. Type name: " + order.gild + ".");
                            try {
                                execute(sendOrder(order, hostId));
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                            order.isPosted = true;
                            break;
                    }
                }
            }
        };
        timer.schedule(periodOrderCheck, 100, 1000);
    }

    @Override
    public String getBotUsername() {
        return "OrderCheckBot";
    }

    @Override
    public String getBotToken() {
        return "5452472435:AAHncFPRKrEmNDPhnj3MSwZtRLTThDvMA1U";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var msg = update.getMessage();
            var id = msg.getChatId();
            //sendText(hostId, "Msg got, id: " + id.toString());

            //Добавление в команду кухни того, кто пишет боту впервые и выключение для него оповещений по умолчанию
            team.putIfAbsent(id, "relax");

            /*Обработка объекта update в зависимости от типа полученных данных*/

            if (msg.isCommand()) {
                if (msg.getText().equals("/start")) {
                    try {
                        execute(sendInlineKeyboardMessage(id));
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                } else if (msg.getText().equals("/stop")) {
                    team.put(id, "relax");
                    sendText(id, "shift stopped");
                }
            }
        }else if (update.hasCallbackQuery()) {
            String call_data = update.getCallbackQuery().getData();
            String[] contents = call_data.split(" "); //call_data must be a string, which contains to words, separated by a space
            Long id = update.getCallbackQuery().getMessage().getChatId();

            switch (contents[0]) {
                case "gild":
                    team.put(id, contents[1]);
                    sendText(hostId, "call_data: " + contents[1] + " is set for " + id + ". (username: " + update.getCallbackQuery().getFrom().getUserName() +")");
                    break;
                case "order":
                    EditMessageText editMessage = new EditMessageText();
                    editMessage.setChatId(String.valueOf(id));
                    editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                    editMessage.setText("<strike>" + update.getCallbackQuery().getMessage().getText() + "</strike>");
                    editMessage.setParseMode("HTML");

                    try {
                        execute(editMessage);
                    } catch (TelegramApiException ex) { logger.log(Level.WARNING, ex.getMessage());  }
                    break;
            }
        }
    }

    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    private String fileToStr(String filename) {
        try (FileReader fr = new FileReader(filename)){
            int c;
            StringBuilder result = new StringBuilder();

            while((c=fr.read())!=-1){
                result.append((char)c);
            }
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SendMessage sendInlineKeyboardMessage(long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton inlineKeyboardButton0 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton5 = new InlineKeyboardButton();

        List<InlineKeyboardButton> kbRow0 = new ArrayList<>();
        List<InlineKeyboardButton> kbRow1 = new ArrayList<>();
        List<InlineKeyboardButton> kbRow2 = new ArrayList<>();
        List<InlineKeyboardButton> kbRow3 = new ArrayList<>();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        inlineKeyboardButton0.setText("Все заказы");
        inlineKeyboardButton0.setCallbackData("gild all");
        kbRow0.add(inlineKeyboardButton0);
        rowList.add(kbRow0);


        inlineKeyboardButton1.setText("Холодка");
        inlineKeyboardButton1.setCallbackData("gild cold");
        kbRow1.add(inlineKeyboardButton1);
        
        inlineKeyboardButton2.setText("Горячий цех");
        inlineKeyboardButton2.setCallbackData("gild hot");
        kbRow1.add(inlineKeyboardButton2);
        rowList.add(kbRow1);

        inlineKeyboardButton3.setText("Картошечная");
        inlineKeyboardButton3.setCallbackData("gild free");
        kbRow2.add(inlineKeyboardButton3);

        inlineKeyboardButton4.setText("Пиццацех");
        inlineKeyboardButton4.setCallbackData("gild pizza");
        kbRow2.add(inlineKeyboardButton4);
        rowList.add(kbRow2);

        inlineKeyboardButton5.setText("Раздача");
        inlineKeyboardButton5.setCallbackData("gild boss");
        kbRow3.add(inlineKeyboardButton5);
        rowList.add(kbRow3);
        
        inlineKeyboardMarkup.setKeyboard(rowList);

        SendMessage sm = new SendMessage();
        sm.setChatId(String.valueOf(chatId));
        sm.setText("Gild choice");
        sm.setReplyMarkup(inlineKeyboardMarkup);

        return sm;
    }

    public void getOrders(){
        String s = fileToStr(ordersListTxt);

        if (s.isEmpty()) {
            return;
        }

        List<String> rows = List.of(s.split("\n"));
        List<List<String>> sOrders = new ArrayList<>();
        for (String row: rows) {
            sOrders.add(List.of(row.split(" ")));
        }

        try {
            for (List<String> row : sOrders) {
                int id = Integer.parseInt(row.get(0));
                String name = row.get(1);
                String gild = row.get(2);
                int amount = Integer.parseInt(row.get(3));
                int table = Integer.parseInt(row.get(4));
                long millis = Long.parseLong(row.get(5));
                boolean isPosted = false;
                boolean isServed = false;

                Order order = new Order(id, name, gild, amount, table, millis, isPosted, isServed);
                orders.add(order);
            }
        } catch (NumberFormatException e){
            logger.log(Level.WARNING, e.getMessage());
        }

        try {
            File f = new File(ordersListTxt);
            if (!f.exists()) {
                System.out.println("File not found: " + ordersListTxt);
                return;
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(f.getAbsoluteFile(), false));
                //Auto clearing the file and "writing".
            bw.close();
        } catch (IOException e) { logger.log(Level.WARNING, e.getMessage()); }
    }

    private SendMessage sendOrder(Order order, long chatId){
        Calendar now = Calendar.getInstance();
        String h = String.format("%02d:", now.get(Calendar.HOUR_OF_DAY));
        String m = String.format("%02d", now.get(Calendar.MINUTE));
        String txt = order.name + " x" + order.amount + ", стол #" + order.table + ". Отображено в: " + h + m;

        InlineKeyboardMarkup ikm = new InlineKeyboardMarkup();
        InlineKeyboardButton ikb = new InlineKeyboardButton();
        ikb.setText("Отдано");
        ikb.setCallbackData("order " + order.id);

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        row.add(ikb);
        rows.add(row);

        ikm.setKeyboard(rows);

        SendMessage sm = new SendMessage();
        sm.setChatId(String.valueOf(chatId));
        sm.setText(txt);
        sm.setReplyMarkup(ikm);
        return sm;
    }
}