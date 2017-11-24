package tv.live.bx.common;

import javax.mail.Authenticator;


public class PasswordAuthentication extends Authenticator {

	@Override
	protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
		return new javax.mail.PasswordAuthentication(Consts.LOG_SENDEMAIL_NAME,
				Consts.LOG_SENDEMAIL_PWD);
	}

}
