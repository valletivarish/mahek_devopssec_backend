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

        // --- Categories ---
        Category conference = categoryRepository.save(Category.builder()
                .name("Conference")
                .description("Large-scale professional conferences and summits")
                .colorCode("#3B82F6")
                .build());

        Category workshop = categoryRepository.save(Category.builder()
                .name("Workshop")
                .description("Hands-on learning sessions and skill-building workshops")
                .colorCode("#10B981")
                .build());

        Category seminar = categoryRepository.save(Category.builder()
                .name("Seminar")
                .description("Educational seminars and lecture-style events")
                .colorCode("#F59E0B")
                .build());

        Category social = categoryRepository.save(Category.builder()
                .name("Social")
                .description("Networking events, meetups, and social gatherings")
                .colorCode("#EF4444")
                .build());

        categoryRepository.save(Category.builder()
                .name("Webinar")
                .description("Online virtual events and presentations")
                .colorCode("#8B5CF6")
                .build());

        // --- Events ---
        Event techConf = eventRepository.save(Event.builder()
                .title("Tech Innovation Summit 2026")
                .description("A two-day conference exploring the latest trends in AI, cloud computing, and cybersecurity.")
                .eventDate(LocalDate.of(2026, 4, 15))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Dublin Convention Centre, Dublin")
                .capacity(200)
                .status(EventStatus.UPCOMING)
                .organizer(admin)
                .category(conference)
                .build());

        Event reactWorkshop = eventRepository.save(Event.builder()
                .title("React & Spring Boot Workshop")
                .description("Hands-on workshop building a full-stack application with React 18 and Spring Boot 3.")
                .eventDate(LocalDate.of(2026, 4, 20))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(16, 0))
                .location("TU Dublin, Grangegorman Campus")
                .capacity(50)
                .status(EventStatus.UPCOMING)
                .organizer(john)
                .category(workshop)
                .build());

        Event devSecOpsSeminar = eventRepository.save(Event.builder()
                .title("DevSecOps Best Practices Seminar")
                .description("Learn how to integrate security into your CI/CD pipeline with practical examples.")
                .eventDate(LocalDate.of(2026, 3, 25))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Online - Zoom")
                .capacity(100)
                .status(EventStatus.UPCOMING)
                .organizer(sarah)
                .category(seminar)
                .build());

        Event networkingMeetup = eventRepository.save(Event.builder()
                .title("Spring Networking Meetup")
                .description("Casual networking event for tech professionals in Dublin. Food and drinks provided.")
                .eventDate(LocalDate.of(2026, 4, 5))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(21, 0))
                .location("The Marker Hotel, Grand Canal Square")
                .capacity(75)
                .status(EventStatus.UPCOMING)
                .organizer(admin)
                .category(social)
                .build());

        // Completed events for forecasting
        Event pastConf1 = eventRepository.save(Event.builder()
                .title("Cloud Computing Forum 2025")
                .description("Annual cloud computing conference covering AWS, Azure, and GCP.")
                .eventDate(LocalDate.of(2025, 11, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("RDS, Dublin")
                .capacity(150)
                .status(EventStatus.COMPLETED)
                .organizer(admin)
                .category(conference)
                .build());

        Event pastConf2 = eventRepository.save(Event.builder()
                .title("AI & ML Workshop 2025")
                .description("Hands-on workshop on building ML models with Python and TensorFlow.")
                .eventDate(LocalDate.of(2025, 12, 5))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(15, 0))
                .location("DCU Innovation Campus")
                .capacity(60)
                .status(EventStatus.COMPLETED)
                .organizer(john)
                .category(workshop)
                .build());

        Event pastConf3 = eventRepository.save(Event.builder()
                .title("Kubernetes Deep Dive 2026")
                .description("Advanced Kubernetes topics including service mesh, operators, and security.")
                .eventDate(LocalDate.of(2026, 1, 20))
                .startTime(LocalTime.of(9, 30))
                .endTime(LocalTime.of(16, 30))
                .location("Science Gallery, Trinity College Dublin")
                .capacity(80)
                .status(EventStatus.COMPLETED)
                .organizer(sarah)
                .category(seminar)
                .build());

        Event pastConf4 = eventRepository.save(Event.builder()
                .title("Open Source Contributors Meetup")
                .description("Celebrating open source contributions with talks and awards.")
                .eventDate(LocalDate.of(2026, 2, 14))
                .startTime(LocalTime.of(17, 0))
                .endTime(LocalTime.of(20, 0))
                .location("Dogpatch Labs, CHQ Building")
                .capacity(100)
                .status(EventStatus.COMPLETED)
                .organizer(admin)
                .category(social)
                .build());

        // --- Attendees ---
        Attendee alice = attendeeRepository.save(Attendee.builder()
                .firstName("Alice").lastName("Murphy").email("alice.murphy@email.com")
                .phone("+353-85-1234567").organization("Google Ireland").build());

        Attendee bob = attendeeRepository.save(Attendee.builder()
                .firstName("Bob").lastName("O'Brien").email("bob.obrien@email.com")
                .phone("+353-86-2345678").organization("Microsoft Ireland").build());

        Attendee carol = attendeeRepository.save(Attendee.builder()
                .firstName("Carol").lastName("Walsh").email("carol.walsh@email.com")
                .phone("+353-87-3456789").organization("AWS Dublin").build());

        Attendee david = attendeeRepository.save(Attendee.builder()
                .firstName("David").lastName("Kelly").email("david.kelly@email.com")
                .phone("+353-89-4567890").organization("Stripe Dublin").build());

        Attendee emma = attendeeRepository.save(Attendee.builder()
                .firstName("Emma").lastName("Ryan").email("emma.ryan@email.com")
                .phone("+353-83-5678901").organization("Intercom").build());

        Attendee frank = attendeeRepository.save(Attendee.builder()
                .firstName("Frank").lastName("Byrne").email("frank.byrne@email.com")
                .phone("+353-85-6789012").organization("Accenture").build());

        // --- RSVPs for upcoming events ---
        rsvpRepository.save(Rsvp.builder().event(techConf).attendee(alice).status(RsvpStatus.CONFIRMED)
                .dietaryPreferences("Vegetarian").specialRequirements("").build());
        rsvpRepository.save(Rsvp.builder().event(techConf).attendee(bob).status(RsvpStatus.CONFIRMED)
                .dietaryPreferences("None").specialRequirements("Wheelchair access").build());
        rsvpRepository.save(Rsvp.builder().event(techConf).attendee(carol).status(RsvpStatus.MAYBE)
                .dietaryPreferences("Gluten-free").specialRequirements("").build());
        rsvpRepository.save(Rsvp.builder().event(techConf).attendee(david).status(RsvpStatus.CONFIRMED)
                .dietaryPreferences("Vegan").specialRequirements("").build());

        rsvpRepository.save(Rsvp.builder().event(reactWorkshop).attendee(alice).status(RsvpStatus.CONFIRMED)
                .dietaryPreferences("").specialRequirements("").build());
        rsvpRepository.save(Rsvp.builder().event(reactWorkshop).attendee(emma).status(RsvpStatus.CONFIRMED)
                .dietaryPreferences("None").specialRequirements("").build());
        rsvpRepository.save(Rsvp.builder().event(reactWorkshop).attendee(frank).status(RsvpStatus.DECLINED)
                .dietaryPreferences("").specialRequirements("").build());

        rsvpRepository.save(Rsvp.builder().event(devSecOpsSeminar).attendee(bob).status(RsvpStatus.CONFIRMED)
                .dietaryPreferences("").specialRequirements("").build());
        rsvpRepository.save(Rsvp.builder().event(devSecOpsSeminar).attendee(carol).status(RsvpStatus.CONFIRMED)
                .dietaryPreferences("").specialRequirements("").build());
        rsvpRepository.save(Rsvp.builder().event(devSecOpsSeminar).attendee(david).status(RsvpStatus.WAITLISTED)
                .dietaryPreferences("").specialRequirements("").build());

        rsvpRepository.save(Rsvp.builder().event(networkingMeetup).attendee(alice).status(RsvpStatus.CONFIRMED)
                .dietaryPreferences("").specialRequirements("").build());
        rsvpRepository.save(Rsvp.builder().event(networkingMeetup).attendee(emma).status(RsvpStatus.CONFIRMED)
                .dietaryPreferences("").specialRequirements("").build());
        rsvpRepository.save(Rsvp.builder().event(networkingMeetup).attendee(frank).status(RsvpStatus.CONFIRMED)
                .dietaryPreferences("").specialRequirements("").build());

        // --- RSVPs for completed events (needed for forecasting) ---
        rsvpRepository.save(Rsvp.builder().event(pastConf1).attendee(alice).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastConf1).attendee(bob).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastConf1).attendee(carol).status(RsvpStatus.CONFIRMED).build());

        rsvpRepository.save(Rsvp.builder().event(pastConf2).attendee(david).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastConf2).attendee(emma).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastConf2).attendee(frank).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastConf2).attendee(alice).status(RsvpStatus.CONFIRMED).build());

        rsvpRepository.save(Rsvp.builder().event(pastConf3).attendee(bob).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastConf3).attendee(carol).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastConf3).attendee(david).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastConf3).attendee(emma).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastConf3).attendee(frank).status(RsvpStatus.CONFIRMED).build());

        rsvpRepository.save(Rsvp.builder().event(pastConf4).attendee(alice).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastConf4).attendee(bob).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastConf4).attendee(carol).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastConf4).attendee(david).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastConf4).attendee(emma).status(RsvpStatus.CONFIRMED).build());
        rsvpRepository.save(Rsvp.builder().event(pastConf4).attendee(frank).status(RsvpStatus.CONFIRMED).build());

        // Check-ins for completed events (increasing trend: 3, 4, 5, 6)
        checkInRepository.save(CheckIn.builder().event(pastConf1).attendee(alice).checkInMethod(CheckInMethod.QR_CODE).build());
        checkInRepository.save(CheckIn.builder().event(pastConf1).attendee(bob).checkInMethod(CheckInMethod.MANUAL).build());
        checkInRepository.save(CheckIn.builder().event(pastConf1).attendee(carol).checkInMethod(CheckInMethod.QR_CODE).build());

        checkInRepository.save(CheckIn.builder().event(pastConf2).attendee(david).checkInMethod(CheckInMethod.QR_CODE).build());
        checkInRepository.save(CheckIn.builder().event(pastConf2).attendee(emma).checkInMethod(CheckInMethod.MANUAL).build());
        checkInRepository.save(CheckIn.builder().event(pastConf2).attendee(frank).checkInMethod(CheckInMethod.QR_CODE).build());
        checkInRepository.save(CheckIn.builder().event(pastConf2).attendee(alice).checkInMethod(CheckInMethod.QR_CODE).build());

        checkInRepository.save(CheckIn.builder().event(pastConf3).attendee(bob).checkInMethod(CheckInMethod.MANUAL).build());
        checkInRepository.save(CheckIn.builder().event(pastConf3).attendee(carol).checkInMethod(CheckInMethod.QR_CODE).build());
        checkInRepository.save(CheckIn.builder().event(pastConf3).attendee(david).checkInMethod(CheckInMethod.QR_CODE).build());
        checkInRepository.save(CheckIn.builder().event(pastConf3).attendee(emma).checkInMethod(CheckInMethod.MANUAL).build());
        checkInRepository.save(CheckIn.builder().event(pastConf3).attendee(frank).checkInMethod(CheckInMethod.QR_CODE).build());

        checkInRepository.save(CheckIn.builder().event(pastConf4).attendee(alice).checkInMethod(CheckInMethod.QR_CODE).build());
        checkInRepository.save(CheckIn.builder().event(pastConf4).attendee(bob).checkInMethod(CheckInMethod.MANUAL).build());
        checkInRepository.save(CheckIn.builder().event(pastConf4).attendee(carol).checkInMethod(CheckInMethod.QR_CODE).build());
        checkInRepository.save(CheckIn.builder().event(pastConf4).attendee(david).checkInMethod(CheckInMethod.QR_CODE).build());
        checkInRepository.save(CheckIn.builder().event(pastConf4).attendee(emma).checkInMethod(CheckInMethod.MANUAL).build());
        checkInRepository.save(CheckIn.builder().event(pastConf4).attendee(frank).checkInMethod(CheckInMethod.QR_CODE).build());

        System.out.println("Database seeded successfully!");
        System.out.println("  - 3 Users (admin/admin123, john/john123, sarah/sarah123)");
        System.out.println("  - 5 Categories");
        System.out.println("  - 8 Events (4 upcoming, 4 completed)");
        System.out.println("  - 6 Attendees");
        System.out.println("  - 18+ RSVPs");
        System.out.println("  - 18 Check-ins (for forecasting)");
    }
}
