package com.supportai.config;

import com.supportai.model.*;
import com.supportai.repository.FaqArticleRepository;
import com.supportai.repository.TicketMessageRepository;
import com.supportai.repository.TicketRepository;
import com.supportai.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FaqArticleRepository faqRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketMessageRepository messageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            logger.info("Seeding initial demo data for SupportAI platform...");

            // 1. Seed Demo Users
            User admin = userRepository.save(new User(
                    "admin@supportai.com",
                    passwordEncoder.encode("password123"),
                    "Admin Officer",
                    Role.ROLE_ADMIN
            ));

            User agent = userRepository.save(new User(
                    "agent@supportai.com",
                    passwordEncoder.encode("password123"),
                    "Sarah Miller (Support Lead)",
                    Role.ROLE_AGENT
            ));

            User customer = userRepository.save(new User(
                    "customer@supportai.com",
                    passwordEncoder.encode("password123"),
                    "Alex Johnson",
                    Role.ROLE_CUSTOMER
            ));

            logger.info("Users seeded: admin@supportai.com, agent@supportai.com, customer@supportai.com (password: password123)");

            // 2. Seed FAQ Articles
            faqRepository.save(new FaqArticle(
                    "How to Reset Your Account Password",
                    "To reset your password, visit the login screen and click 'Forgot Password'. Enter your registered email address and you will receive a secure one-time password (OTP) verification link within 60 seconds. Make sure your new password is at least 8 characters with numbers and symbols.",
                    TicketCategory.ACCOUNT_ACCESS,
                    "password, reset, login, auth, credential"
            ));

            faqRepository.save(new FaqArticle(
                    "Billing Refund and Proration Policy",
                    "We offer a 30-day money-back guarantee for all monthly and annual subscriptions. If you notice an unexpected or duplicate charge, our billing team can instantly issue a full refund to your original payment method. Prorated credits are automatically calculated when switching plans midway.",
                    TicketCategory.BILLING,
                    "billing, refund, invoice, charge, duplicate, subscription"
            ));

            faqRepository.save(new FaqArticle(
                    "Setting Up Two-Factor Authentication (2FA)",
                    "Navigate to Settings > Security > Two-Factor Authentication. Scan the displayed QR code with Google Authenticator, Authy, or 1Password. Save your 10 backup recovery codes in a safe place. 2FA is required for all workspace admin actions.",
                    TicketCategory.ACCOUNT_ACCESS,
                    "2fa, mfa, security, authenticator, otp, login"
            ));

            faqRepository.save(new FaqArticle(
                    "API Rate Limits and Webhook Best Practices",
                    "The standard API rate limit is 1,000 requests per minute per API key. Webhook listeners must return an HTTP 200 OK within 5,000ms. Failed webhooks are retried with exponential backoff up to 5 times (1m, 5m, 15m, 1h, 6h).",
                    TicketCategory.TECHNICAL_SUPPORT,
                    "api, rate limit, webhook, 429, timeout, integration, 500"
            ));

            faqRepository.save(new FaqArticle(
                    "Upgrading or Downgrading Workspace Subscription Plans",
                    "Workspace owners can change tiers at any time in Workspace Settings > Billing. Upgrades apply immediately with prorated charge for the remaining billing cycle. Downgrades take effect at the end of the current period.",
                    TicketCategory.BILLING,
                    "subscription, upgrade, downgrade, plan, pricing, tier"
            ));

            // 3. Seed Realistic Sample Tickets
            Ticket ticket1 = new Ticket();
            ticket1.setTicketNumber("TCK-20260901-A1B2");
            ticket1.setTitle("Charged twice on credit card for Pro Plan");
            ticket1.setDescription("I was charged $49.00 twice on September 1st on my Visa card ending in 4112. This is unacceptable, please refund the extra charge immediately!");
            ticket1.setCategory(TicketCategory.BILLING);
            ticket1.setPriority(TicketPriority.URGENT);
            ticket1.setStatus(TicketStatus.ESCALATED);
            ticket1.setSentimentScore(-0.75);
            ticket1.setSentimentLabel(SentimentType.FRUSTRATED);
            ticket1.setCustomer(customer);
            ticket1.setAssignedAgent(agent);
            ticket1.setEscalatedAt(LocalDateTime.now().minusHours(2));
            ticket1 = ticketRepository.save(ticket1);

            messageRepository.save(new TicketMessage(
                    ticket1,
                    customer.getId(),
                    SenderType.CUSTOMER,
                    customer.getFullName(),
                    ticket1.getDescription(),
                    SentimentType.FRUSTRATED
            ));

            messageRepository.save(new TicketMessage(
                    ticket1,
                    null,
                    SenderType.AI_BOT,
                    "SupportAI Assistant",
                    "I sincerely apologize for the duplicate charge on your account. I have analyzed your billing history, classified this under BILLING with URGENT priority, and escalated your ticket directly to our lead support team.",
                    SentimentType.NEUTRAL
            ));

            messageRepository.save(new TicketMessage(
                    ticket1,
                    agent.getId(),
                    SenderType.AGENT,
                    agent.getFullName(),
                    "Hello Alex, I see the duplicate charge in our Stripe ledger. I have initiated an immediate refund of $49.00. You should see it reflected on your card statement within 2-3 business days.",
                    SentimentType.POSITIVE
            ));

            // Ticket 2: Tech support
            Ticket ticket2 = new Ticket();
            ticket2.setTicketNumber("TCK-20260902-C3D4");
            ticket2.setTitle("Webhook delivery failing with HTTP 504 Gateway Timeout");
            ticket2.setDescription("Our production receiving server is occasionally seeing 504 timeouts when your webhook dispatch triggers. Can you check retry latency?");
            ticket2.setCategory(TicketCategory.TECHNICAL_SUPPORT);
            ticket2.setPriority(TicketPriority.HIGH);
            ticket2.setStatus(TicketStatus.IN_PROGRESS);
            ticket2.setSentimentScore(-0.25);
            ticket2.setSentimentLabel(SentimentType.NEGATIVE);
            ticket2.setCustomer(customer);
            ticket2.setAssignedAgent(agent);
            ticket2 = ticketRepository.save(ticket2);

            messageRepository.save(new TicketMessage(
                    ticket2,
                    customer.getId(),
                    SenderType.CUSTOMER,
                    customer.getFullName(),
                    ticket2.getDescription(),
                    SentimentType.NEGATIVE
            ));

            messageRepository.save(new TicketMessage(
                    ticket2,
                    null,
                    SenderType.AI_BOT,
                    "SupportAI Assistant",
                    "According to our API documentation, webhook listeners must respond within 5,000ms. If you are doing heavy processing, consider queuing the event and responding with 200 OK immediately.",
                    SentimentType.POSITIVE
            ));

            // Ticket 3: Feature request (Resolved)
            Ticket ticket3 = new Ticket();
            ticket3.setTicketNumber("TCK-20260903-E5F6");
            ticket3.setTitle("Add support for CSV exports in ticket analytics");
            ticket3.setDescription("We love the analytics dashboard! It would be fantastic if we could export the monthly ticket volume and sentiment trends directly to CSV.");
            ticket3.setCategory(TicketCategory.FEATURE_REQUEST);
            ticket3.setPriority(TicketPriority.LOW);
            ticket3.setStatus(TicketStatus.RESOLVED);
            ticket3.setSentimentScore(0.85);
            ticket3.setSentimentLabel(SentimentType.POSITIVE);
            ticket3.setCustomer(customer);
            ticket3.setAssignedAgent(agent);
            ticket3 = ticketRepository.save(ticket3);

            messageRepository.save(new TicketMessage(
                    ticket3,
                    customer.getId(),
                    SenderType.CUSTOMER,
                    customer.getFullName(),
                    ticket3.getDescription(),
                    SentimentType.POSITIVE
            ));

            messageRepository.save(new TicketMessage(
                    ticket3,
                    agent.getId(),
                    SenderType.AGENT,
                    agent.getFullName(),
                    "Hi Alex, great news! We just rolled out CSV analytics export in v2.4. You can click 'Export CSV' in the top right of the dashboard.",
                    SentimentType.POSITIVE
            ));

            logger.info("Sample tickets and messages seeded successfully!");
        }
    }
}
