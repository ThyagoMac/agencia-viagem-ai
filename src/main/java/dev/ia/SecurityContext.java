package dev.ia;

public final class SecurityContext {

  private static final ThreadLocal<String> currentUser = new ThreadLocal<>();

  private SecurityContext() {
  }

  public static void setCurrentUser(String userName) {
    if (userName == null || userName.isBlank()) {
      throw new IllegalArgumentException("Nome de usuário inválido");
    }
    currentUser.set(userName.trim());
  }

  public static String getCurrentUser() {
    return currentUser.get();
  }

  public static void clear() {
    currentUser.remove();
  }
}
