package com.employeerank.config;

import com.employeerank.entity.Company;
import com.employeerank.entity.User;
import com.employeerank.enums.Role;
import com.employeerank.repository.CompanyRepository;
import com.employeerank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedSampleCompany();
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail("admin@employeerank.com")) return;

        User admin = User.builder()
                .fullName("System Admin")
                .username("admin")
                .email("admin@employeerank.com")
                .password(passwordEncoder.encode("Admin@1234"))
                .role(Role.ROLE_ADMIN)
                .isActive(true)
                .isPublicProfile(false)
                .totalCredits(0)
                .jobTitle("Platform Administrator")
                .build();

        userRepository.save(admin);
        log.info("✅ Admin user seeded → admin@employeerank.com / Admin@1234");
    }

    private void seedSampleCompany() {
        if (companyRepository.existsByName("EmployeeRank Demo Corp")) return;

        // Create a demo company admin user
        User companyAdmin;
        if (!userRepository.existsByEmail("company@demo.com")) {
            companyAdmin = User.builder()
                    .fullName("Demo Company Admin")
                    .username("democompany")
                    .email("company@demo.com")
                    .password(passwordEncoder.encode("Company@1234"))
                    .role(Role.ROLE_COMPANY)
                    .isActive(true)
                    .isPublicProfile(false)
                    .totalCredits(0)
                    .build();
            companyAdmin = userRepository.save(companyAdmin);
        } else {
            companyAdmin = userRepository.findByEmail("company@demo.com").orElseThrow();
        }

        Company company = Company.builder()
                .name("EmployeeRank Demo Corp")
                .description("A demonstration company showcasing the EmployeeRank platform features.")
                .industry("Technology")
                .location("Bangalore, India")
                .companySize("51-200")
                .websiteUrl("https://employeerank.com")
                .isVerified(true)
                .isHiring(true)
                .minCreditThreshold(50)
                .adminUser(companyAdmin)
                .build();

        Company savedCompany = companyRepository.save(company);

        // Create sample manager
        if (!userRepository.existsByEmail("manager@demo.com")) {
            User manager = User.builder()
                    .fullName("Demo Manager")
                    .username("demomanager")
                    .email("manager@demo.com")
                    .password(passwordEncoder.encode("Manager@1234"))
                    .role(Role.ROLE_MANAGER)
                    .isActive(true)
                    .isPublicProfile(true)
                    .totalCredits(320)
                    .jobTitle("Engineering Manager")
                    .department("Engineering")
                    .company(savedCompany)
                    .skills("Java, Spring Boot, Leadership, Agile")
                    .yearsOfExperience(8)
                    .build();
            userRepository.save(manager);
        }

        // Create sample employees
        String[][] sampleEmployees = {
            {"Alice Johnson", "alice_j", "alice@demo.com", "Software Engineer", "Engineering", "Java, Spring Boot, React", "4"},
            {"Bob Smith", "bob_s", "bob@demo.com", "Product Designer", "Design", "Figma, UI/UX, Prototyping", "3"},
            {"Carol White", "carol_w", "carol@demo.com", "Data Analyst", "Analytics", "Python, SQL, Tableau", "5"},
            {"Dave Brown", "dave_b", "dave@demo.com", "DevOps Engineer", "Infrastructure", "Docker, Kubernetes, AWS", "6"}
        };

        for (String[] emp : sampleEmployees) {
            if (!userRepository.existsByEmail(emp[2])) {
                User employee = User.builder()
                        .fullName(emp[0])
                        .username(emp[1])
                        .email(emp[2])
                        .password(passwordEncoder.encode("Employee@1234"))
                        .role(Role.ROLE_EMPLOYEE)
                        .isActive(true)
                        .isPublicProfile(true)
                        .totalCredits((int)(Math.random() * 300) + 100)
                        .jobTitle(emp[3])
                        .department(emp[4])
                        .company(savedCompany)
                        .skills(emp[5])
                        .yearsOfExperience(Integer.parseInt(emp[6]))
                        .build();
                userRepository.save(employee);
            }
        }

        log.info("✅ Sample company and employees seeded");
        log.info("   → company@demo.com / Company@1234");
        log.info("   → manager@demo.com / Manager@1234");
        log.info("   → alice@demo.com   / Employee@1234");
        log.info("   → bob@demo.com     / Employee@1234");
    }
}
