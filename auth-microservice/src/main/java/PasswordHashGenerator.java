import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "admin123";
        String hash = encoder.encode(password);
        
        System.out.println("===========================================");
        System.out.println("Password: " + password);
        System.out.println("Hash BCrypt: " + hash);
        System.out.println("===========================================");
        System.out.println("\nSQL pentru inserare:");
        System.out.println("INSERT INTO auth_users (id, username, email, password_hash, role, created_at, updated_at)");
        System.out.println("VALUES (UUID(), 'admin', 'admin@energy.ro', '" + hash + "', 'ADMIN', NOW(), NOW());");
    }
}
