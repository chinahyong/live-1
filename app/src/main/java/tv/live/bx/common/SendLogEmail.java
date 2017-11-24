package tv.live.bx.common;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.util.Log;

/**
 * 
 * @ClassName: SendLogEmail
 * @Description: TODO 发送报错的log日志到邮件
 * @author Lisper
 * @date 2014-12-2 下午2:07:28
 * 
 */
public class SendLogEmail {
	private String mailServerHost = "smtp.ym.163.com";
	private String mailServerPort = "25";

	/**
	 * 
	 * @Title: getProperties
	 * @Description: TODO 获得邮件会话属性
	 * @author Lisper
	 * @return
	 */
	public Properties getProperties() {
		Properties p = new Properties();
		p.put("mail.smtp.protocol", "smtp");
		p.put("mail.smtp.host", this.mailServerHost);
		p.put("mail.smtp.port", this.mailServerPort);
		p.put("mail.smtp.auth", "true");
		return p;
	}

	public boolean sendTextMail(String title, String content) {
		Log.e("sendTextMail", content);
		// 判断是否需要身份认证
		PasswordAuthentication authenticator = null;
		Properties pro = getProperties();
		authenticator = new PasswordAuthentication();
		// 根据邮件会话属性和密码验证器构造一个发送邮件的session
		Session sendMailSession = Session
				.getDefaultInstance(pro, authenticator);
		try {
			// 根据session创建一个邮件消息
			Message mailMessage = new MimeMessage(sendMailSession);
			// 创建邮件发送者地址
			Address from = new InternetAddress(Consts.LOG_SENDEMAIL_NAME);
			// 设置邮件消息的发送者
			mailMessage.setFrom(from);
			// 创建邮件的接收者地址，并设置到邮件消息中
			Address to = new InternetAddress(Consts.LOG_RECIVEMAIL_NAME);
			mailMessage.setRecipient(Message.RecipientType.TO, to);
			// 设置邮件消息的主题
			mailMessage.setSubject(title);
			// 设置邮件消息发送的时间
			mailMessage.setSentDate(new Date());
			// 设置邮件消息的主要内容
			mailMessage.setText(content);
			// 发送邮件
			Transport transport = sendMailSession.getTransport("smtp");
			transport.connect(mailServerHost, Consts.LOG_SENDEMAIL_NAME,
					Consts.LOG_SENDEMAIL_PWD);
			Transport.send(mailMessage);
			transport.close();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

}
