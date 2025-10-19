import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt密码Hash生成工具
 * 运行此程序生成正确的密码hash
 */
public class GenerateBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("=== 生成BCrypt密码Hash ===");
        System.out.println();

        String adminPassword = "admin123";
        String userPassword = "user123";

        String adminHash = encoder.encode(adminPassword);
        String userHash = encoder.encode(userPassword);

        System.out.println("admin123 的 BCrypt Hash:");
        System.out.println(adminHash);
        System.out.println();

        System.out.println("user123 的 BCrypt Hash:");
        System.out.println(userHash);
        System.out.println();

        // 验证
        System.out.println("=== 验证密码 ===");
        System.out.println("验证 admin123: " + encoder.matches(adminPassword, adminHash));
        System.out.println("验证 user123: " + encoder.matches(userPassword, userHash));
        System.out.println();

        // 提供SQL更新语句
        System.out.println("=== SQL更新语句 ===");
        System.out.println("UPDATE user_info SET password = '" + adminHash + "' WHERE username = 'admin';");
        System.out.println("UPDATE user_info SET password = '" + userHash + "' WHERE username = 'user';");
    }
}
