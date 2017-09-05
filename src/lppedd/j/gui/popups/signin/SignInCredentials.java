package lppedd.j.gui.popups.signin;

public class SignInCredentials
{
   private final String hostName;
   private final String user;
   private final String password;

   public SignInCredentials(final String hostName, final String user, final String password) {
      this.hostName = hostName;
      this.user = user;
      this.password = password;
   }
   
   public boolean isValid() {
      return !(hostName.isEmpty() || user.isEmpty());
   }
   
   public String getHostName() {
      return hostName;
   }
   
   public String getUser() {
      return user;
   }
   
   public String getPassword() {
      return password;
   }
}
