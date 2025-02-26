import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;


public class EmailFetcher {
    private String host;
    private String storeType;
    private String username;
    private String password;
    private EmailClassifier model;
    private DatabaseManager dbManager;

    public EmailFetcher(String host, String storeType, String username, String password)throws Exception { 
        this.host = host;
        this.storeType = storeType;
        this.username = username;
        this.password = password;
        this.model = new EmailClassifier();
        this.dbManager = new DatabaseManager();
    }
    

    public void checkEmails()throws Exception {
        try {
            Properties properties = new Properties();
            properties.put("mail.pop3.host", host); // Host should be "pop.gmail.com" for Gmail
            properties.put("mail.pop3.port", "995"); // Secure port for POP3
            properties.put("mail.pop3.ssl.enable", "true"); // Enable SSL
            properties.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // Use SSL
            properties.put("mail.pop3.socketFactory.fallback", "false");
            properties.put("mail.pop3.socketFactory.port", "995");
            
            Session emailSession = Session.getDefaultInstance(properties);
            
            Store store = emailSession.getStore(storeType); // 'pop3s' for secure POP3
            store.connect(host, username, password);

            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            Message[] messages = emailFolder.getMessages();
            int totalEmails = messages.length;
            System.out.println("Total Email: " + totalEmails);

            List<EmailCheckerThread> threads = new ArrayList<>();
            for (int i = 0, n = messages.length; i < n; i++) {
                EmailCheckerThread thread = new EmailCheckerThread(messages[i], model, dbManager);
                thread.setName(String.valueOf(i + 1));
                threads.add(thread);
                thread.start();

                //debug print
                // System.out.println("---------------------------------");
                // System.out.println("Email Number " + (i + 1));
                // System.out.println("Subject: " + message.getSubject());
                // System.out.println("From: " + message.getFrom()[0]);
                // System.out.println("content: " + contentString);
                // System.out.println("Is Spam: " + isSpam);
            }

            int processedEmails = 0;
            long startTime = System.currentTimeMillis();

            for (EmailCheckerThread thread : threads) {
                thread.join();
                processedEmails++;
                System.out.println("Processed " + processedEmails + "/" + totalEmails + " (" + (processedEmails * 100 / totalEmails) + "%)");
            }

            long endTime = System.currentTimeMillis();
            double processingTime = (endTime - startTime) / 1000.0;

            System.out.println("Processing time: " + String.format("%.1f", processingTime) + " seconds");

            emailFolder.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
