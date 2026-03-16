package com.eventmanager.eventrsvp.config;

import com.eventmanager.eventrsvp.model.*;
import com.eventmanager.eventrsvp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final AttendeeRepository attendeeRepository;
    private final RsvpRepository rsvpRepository;
    private final CheckInRepository checkInRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      EventRepository eventRepository,
                      AttendeeRepository attendeeRepository,
                      RsvpRepository rsvpRepository,
                      CheckInRepository checkInRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
        this.attendeeRepository = attendeeRepository;
        this.rsvpRepository = rsvpRepository;
        this.checkInRepository = checkInRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            System.out.println("Database already seeded. Skipping...");
            return;
        }

        System.out.println("Seeding database with sample data...");

        // --- Users ---
        User admin = userRepository.save(User.builder()
                .username("admin")
                .email("admin@eventrsvp.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Admin User")
                .role(UserRole.ADMIN)
                .build());

        User john = userRepository.save(User.builder()
                .username("john")
                .email("john@eventrsvp.com")
                .password(passwordEncoder.encode("john123"))
                .fullName("John Smith")
                .role(UserRole.USER)
                .build());

        User sarah = userRepository.save(User.builder()
                .username("sarah")
                .email("sarah@eventrsvp.com")
                .password(passwordEncoder.encode("sarah123"))
                .fullName("Sarah Johnson")
                .role(UserRole.USER)
                .build());

        User demo = userRepository.save(User.builder()
                .username("demo")
                .email("demo@eventrsvp.com")
                .password(passwordEncoder.encode("demo123"))
                .fullName("Demo User")
                .role(UserRole.USER)
                .build());

        User mike = userRepository.save(User.builder()
                .username("mike")
                .email("mike@eventrsvp.com")
                .password(passwordEncoder.encode("mike123"))
                .fullName("Mike Wilson")
                .role(UserRole.USER)
                .build());

        // --- Attendees linked to non-admin users ---
        Attendee johnAttendee = attendeeRepository.save(Attendee.builder()
                .firstName("John").lastName("Smith").email("john@eventrsvp.com")
                .phone("+353-85-1234567").user(john).build());

        Attendee sarahAttendee = attendeeRepository.save(Attendee.builder()
                .firstName("Sarah").lastName("Johnson").email("sarah@eventrsvp.com")
                .phone("+353-86-2345678").user(sarah).build());

        Attendee demoAttendee = attendeeRepository.save(Attendee.builder()
                .firstName("Demo").lastName("User").email("demo@eventrsvp.com")
                .phone("+353-87-3456789").user(demo).build());

        Attendee mikeAttendee = attendeeRepository.save(Attendee.builder()
                .firstName("Mike").lastName("Wilson").email("mike@eventrsvp.com")
                .phone("+353-89-4567890").user(mike).build());

        // --- Categories ---
        Category conference = categoryRepository.save(Category.builder()
                .name("Conference")
                .description("Professional conferences and summits")
                .colorCode("#3B82F6")
                .build());

        Category workshop = categoryRepository.save(Category.builder()
                .name("Workshop")
                .description("Hands-on learning sessions")
                .colorCode("#10B981")
                .build());

        Category seminar = categoryRepository.save(Category.builder()
                .name("Seminar")
                .description("Educational seminars and lectures")
                .colorCode("#F59E0B")
                .build());

        // --- Events ---
        Event techConf = eventRepository.save(Event.builder()
                .title("Tech Innovation Summit 2026")
                .description("Conference exploring AI, cloud computing, and cybersecurity trends.")
                .eventDate(LocalDate.of(2026, 4, 15))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Dublin Convention Centre")
                .capacity(200)
                .status(EventStatus.UPCOMING)
                .organizer(admin)
                .category(conference)
                .build());

        Event reactWorkshop = eventRepository.save(Event.builder()
                .title("React & Spring Boot Workshop")
                .description("Hands-on workshop building a full-stack application.")
                .eventDate(LocalDate.of(2026, 4, 20))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(16, 0))
                .location("TU Dublin, Grangegorman")
                .capacity(50)
                .status(EventStatus.UPCOMING)
                .organizer(admin)
                .category(workshop)
                .build());

        Event pastEvent = eventRepository.save(Event.builder()
                .title("Cloud Computing Forum 2025")
                .description("Annual cloud computing conference covering AWS, Azure, and GCP.")
                .eventDate(LocalDate.of(2025, 11, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("RDS, Dublin")
                .capacity(150)
                .status(EventStatus.COMPLETED)
                .organizer(admin)
                .category(seminar)
                .build());

        // --- RSVPs ---
        // john and sarah: minimal data (they are demo login buttons — used for live demo)
        rsvpRepository.save(Rsvp.builder().event(techConf).attendee(johnAttendee).status(RsvpStatus.CONFIRMED).build());

        // demo and mike: more data to show populated lists
        rsvpRepository.save(Rsvp.builder().event(techConf).attendee(demoAttendee).status(RsvpStatus.CONFIRMED)
                .dietaryPreferences("Vegetarian").build());
        rsvpRepository.save(Rsvp.builder().event(reactWorkshop).attendee(demoAttendee).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastEvent).attendee(demoAttendee).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(techConf).attendee(mikeAttendee).status(RsvpStatus.CONFIRMED)
                .dietaryPreferences("None").build());
        rsvpRepository.save(Rsvp.builder().event(pastEvent).attendee(mikeAttendee).status(RsvpStatus.CONFIRMED).build());

        // --- Check-ins (completed event) ---
        checkInRepository.save(CheckIn.builder().event(pastEvent).attendee(demoAttendee).checkInMethod(CheckInMethod.QR_CODE).build());
        checkInRepository.save(CheckIn.builder().event(pastEvent).attendee(mikeAttendee).checkInMethod(CheckInMethod.MANUAL).build());

        System.out.println("Database seeded successfully!");
        System.out.println("  - 5 Users (admin/admin123, john/john123, sarah/sarah123, demo/demo123, mike/mike123)");
        System.out.println("  - 4 Attendees (linked to john, sarah, demo, mike)");
        System.out.println("  - 3 Categories");
        System.out.println("  - 3 Events (2 upcoming, 1 completed)");
        System.out.println("  - 6 RSVPs (john: 1, demo: 3, mike: 2)");
        System.out.println("  - 2 Check-ins (demo + mike on past event)");
    }
}
