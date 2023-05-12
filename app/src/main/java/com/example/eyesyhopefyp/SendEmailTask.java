package com.example.eyesyhopefyp;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.eyesyhopefyp.Utility.Voice;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
    private String email;
    private String subject;
    private String message;
    private String fromEmail;
    private String fromEmailPasword;
    private Activity activity;

    private Context context;


    public SendEmailTask(String email, String subject, String message, String fromEmail, String fromEmailPasword, Context context, HelpActivity helpActivity) {
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.fromEmail = fromEmail;
        this.fromEmailPasword = fromEmailPasword;
        this.context = context;
        this.activity=helpActivity;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            // Create a new instance of the JavaMail API's Session object
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");

            Session session = Session.getDefaultInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, fromEmailPasword);
                }
            });

            // Create a new message and set the recipients, subject, and message body
            Message mesage = new MimeMessage(session);
            mesage.setFrom(new InternetAddress(fromEmail));
            mesage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            mesage.setSubject(subject);
            mesage.setText(message);

            // Send the message using the JavaMail API's Transport object
            Transport.send(mesage);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Error in sending Email ",e.getMessage());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, "Email sent successfully", Toast.LENGTH_LONG).show();
            Voice.speak(activity, "Email sent Successfully", false);
        } else {
            Toast.makeText(context, "Failed to send email", Toast.LENGTH_LONG).show();
            Voice.speak(activity, "Failed to send email , try again later", false);
        }
    }
}

