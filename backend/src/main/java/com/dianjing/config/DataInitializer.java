package com.dianjing.config;

import com.dianjing.common.Constants;
import com.dianjing.entity.*;
import com.dianjing.mapper.*;
import com.dianjing.service.LevelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserMapper userMapper;
    private final ServiceMapper serviceMapper;
    private final ServiceTagMapper serviceTagMapper;
    private final ServiceTagRelationMapper serviceTagRelationMapper;
    private final GameAccountMapper gameAccountMapper;
    private final OrderMapper orderMapper;
    private final ReviewMapper reviewMapper;
    private final PasswordEncoder passwordEncoder;
    private final LevelService levelService;

    public DataInitializer(UserMapper userMapper, ServiceMapper serviceMapper,
                           ServiceTagMapper serviceTagMapper, ServiceTagRelationMapper serviceTagRelationMapper,
                           GameAccountMapper gameAccountMapper, OrderMapper orderMapper,
                           ReviewMapper reviewMapper, PasswordEncoder passwordEncoder,
                           LevelService levelService) {
        this.userMapper = userMapper;
        this.serviceMapper = serviceMapper;
        this.serviceTagMapper = serviceTagMapper;
        this.serviceTagRelationMapper = serviceTagRelationMapper;
        this.gameAccountMapper = gameAccountMapper;
        this.orderMapper = orderMapper;
        this.reviewMapper = reviewMapper;
        this.passwordEncoder = passwordEncoder;
        this.levelService = levelService;
    }

    @Override
    public void run(String... args) {
        // 初始化等级体系
        levelService.initializeDefaultLevels();
        log.info("等级体系初始化完成");
        
        // 只在数据库为空时初始化测试数据
        if (userMapper.count() > 0) {
            log.info("数据库已有用户数据，跳过测试数据初始化");
            return;
        }

        log.info("开始初始化测试数据...");

        // 1. 创建管理员用户
        User admin = createAdmin();
        log.info("创建管理员用户: {}", admin.getUsername());

        // 2. 创建测试用户
        User testUser = createUser("testuser", "123456", "13800138000", "test@example.com", Constants.ROLE_USER);
        User testUser2 = createUser("testuser2", "123456", "13800138001", "test2@example.com", Constants.ROLE_USER);
        log.info("创建测试用户: {}, {}", testUser.getUsername(), testUser2.getUsername());

        // 3. 创建测试服务商
        User provider1 = createUser("provider1", "123456", "13900139000", "provider1@example.com", Constants.ROLE_PROVIDER);
        User provider2 = createUser("provider2", "123456", "13900139001", "provider2@example.com", Constants.ROLE_PROVIDER);
        provider1.setBalance(new BigDecimal("1000.00"));
        provider2.setBalance(new BigDecimal("2000.00"));
        userMapper.save(provider1);
        userMapper.save(provider2);
        log.info("创建测试服务商: {}, {}", provider1.getUsername(), provider2.getUsername());

        // 4. 创建服务标签
        ServiceTag tag1 = createTag("高手");
        ServiceTag tag2 = createTag("效率高");
        ServiceTag tag3 = createTag("态度好");
        ServiceTag tag4 = createTag("新手友好");
        ServiceTag tag5 = createTag("国服");
        log.info("创建服务标签: {}, {}, {}, {}, {}", tag1.getName(), tag2.getName(), tag3.getName(), tag4.getName(), tag5.getName());

        // 5. 创建测试服务
        Service service1 = createService(provider1, "LOL", Constants.SERVICE_TYPE_COMPANION,
                "LOL 钻石陪玩", "钻石段位专业陪玩，带你上分", new BigDecimal("30.00"), 60);
        Service service2 = createService(provider1, "LOL", Constants.SERVICE_TYPE_BOOSTING,
                "LOL 代练上分", "大师段位代练，保证胜率", new BigDecimal("50.00"), 90);
        Service service3 = createService(provider2, "王者荣耀", Constants.SERVICE_TYPE_COMPANION,
                "王者荣耀 星耀陪玩", "星耀段位陪玩，欢乐上分", new BigDecimal("25.00"), 60);
        Service service4 = createService(provider2, "和平精英", Constants.SERVICE_TYPE_COMPANION,
                "和平精英 吃鸡陪玩", "王牌段位陪玩，带你吃鸡", new BigDecimal("35.00"), 90);
        Service service5 = createService(provider1, "原神", Constants.SERVICE_TYPE_COMPANION,
                "原神 深渊陪玩", "深渊满星大佬带你通关", new BigDecimal("40.00"), 120);
        log.info("创建测试服务: {}, {}, {}, {}, {}", service1.getTitle(), service2.getTitle(),
                service3.getTitle(), service4.getTitle(), service5.getTitle());

        // 为服务添加标签
        addTagToService(service1.getId(), tag1.getId());
        addTagToService(service1.getId(), tag3.getId());
        addTagToService(service2.getId(), tag1.getId());
        addTagToService(service2.getId(), tag2.getId());
        addTagToService(service3.getId(), tag3.getId());
        addTagToService(service3.getId(), tag4.getId());
        addTagToService(service4.getId(), tag2.getId());
        addTagToService(service4.getId(), tag5.getId());
        addTagToService(service5.getId(), tag1.getId());
        addTagToService(service5.getId(), tag2.getId());

        // 6. 创建测试游戏账号
        createGameAccount(testUser, "LOL", "TestLOL", "钻石");
        createGameAccount(testUser, "王者荣耀", "TestWZRY", "星耀");
        createGameAccount(testUser2, "和平精英", "TestHPJY", "王牌");
        log.info("创建测试游戏账号");

        // 7. 创建测试订单
        Order order1 = createOrder(testUser, provider1, service1, Constants.ORDER_COMPLETED);
        Order order2 = createOrder(testUser, provider2, service3, Constants.ORDER_PENDING_REVIEW);
        Order order3 = createOrder(testUser2, provider1, service2, Constants.ORDER_IN_SERVICE);
        Order order4 = createOrder(testUser2, provider2, service4, Constants.ORDER_PENDING_PAYMENT);
        log.info("创建测试订单");

        // 8. 为完成的订单创建评价
        createReview(order1, testUser, provider1, service1, 5, "服务非常好，技术一流，态度也很好");
        createReview(order1, testUser, provider1, service1, 4, "服务不错，效率很高");
        createReview(order1, testUser, provider1, service1, 5, "非常专业，带我轻松上分");
        createReview(order1, testUser, provider1, service1, 4, "服务态度很好，技术也可以");
        createReview(order1, testUser, provider1, service1, 5, "超级棒的陪玩体验");
        createReview(order1, testUser, provider1, service1, 4, "很满意的一次服务");
        createReview(order1, testUser, provider1, service1, 5, "技术很强，态度很好");
        createReview(order1, testUser, provider1, service1, 4, "不错的陪玩，效率很高");
        createReview(order1, testUser, provider1, service1, 5, "非常专业，值得推荐");
        createReview(order1, testUser, provider1, service1, 4, "服务很好，态度也不错");
        log.info("创建测试评价");

        // 9. 基于真实数据计算服务的评分、评价数和销量
        updateServiceStats(service1);
        updateServiceStats(service2);
        updateServiceStats(service3);
        updateServiceStats(service4);
        updateServiceStats(service5);

        log.info("测试数据初始化完成！");
    }

    private User createAdmin() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Constants.ROLE_ADMIN);
        admin.setStatus(Constants.STATUS_NORMAL);
        admin.setBalance(BigDecimal.ZERO);
        return userMapper.save(admin);
    }

    private User createUser(String username, String password, String phone, String email, int role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(Constants.STATUS_NORMAL);
        user.setBalance(BigDecimal.ZERO);
        user.setCreditScore(100);
        return userMapper.save(user);
    }

    private ServiceTag createTag(String name) {
        ServiceTag tag = new ServiceTag();
        tag.setName(name);
        return serviceTagMapper.save(tag);
    }

    private Service createService(User provider, String gameType, int serviceType,
                                   String title, String description, BigDecimal price, int duration) {
        Service service = new Service();
        service.setProviderId(provider.getId());
        service.setGameType(gameType);
        service.setServiceType(serviceType);
        service.setTitle(title);
        service.setDescription(description);
        service.setPrice(price);
        service.setDuration(duration);
        service.setStatus(Constants.SERVICE_ONLINE);
        service.setRating(BigDecimal.ZERO);
        service.setReviewCount(0);
        service.setSalesCount(0);
        return serviceMapper.save(service);
    }

    private void addTagToService(Long serviceId, Long tagId) {
        ServiceTagRelation relation = new ServiceTagRelation();
        relation.setServiceId(serviceId);
        relation.setTagId(tagId);
        serviceTagRelationMapper.save(relation);
    }

    private void createGameAccount(User user, String gameType, String accountName, String level) {
        GameAccount account = new GameAccount();
        account.setUserId(user.getId());
        account.setGameType(gameType);
        account.setAccountName(accountName);
        account.setAccountLevel(level);
        gameAccountMapper.save(account);
    }

    private Order createOrder(User user, User provider, Service service, int status) {
        Order order = new Order();
        order.setUserId(user.getId());
        order.setProviderId(provider.getId());
        order.setServiceId(service.getId());
        order.setOrderNo(com.dianjing.util.OrderNoGenerator.generate());
        order.setTotalAmount(service.getPrice());
        order.setStatus(status);
        order.setRequirements("请认真对待每一局游戏");
        return orderMapper.save(order);
    }

    private void createReview(Order order, User user, User provider, Service service, int rating, String content) {
        Review review = new Review();
        review.setOrderId(order.getId());
        review.setUserId(user.getId());
        review.setProviderId(provider.getId());
        review.setServiceId(service.getId());
        review.setRating(rating);
        review.setContent(content);
        reviewMapper.save(review);
    }

    private void updateServiceStats(Service service) {
        // 计算销量：已完成的订单数量
        long salesCount = orderMapper.countByProviderIdAndStatus(service.getProviderId(), Constants.ORDER_COMPLETED);
        
        // 计算评价数和平均评分
        long reviewCount = reviewMapper.countByServiceId(service.getId());
        Double averageRating = reviewMapper.getAverageRatingByService(service.getId());
        
        // 更新服务统计数据
        service.setSalesCount((int) salesCount);
        service.setReviewCount((int) reviewCount);
        service.setRating(averageRating != null ? BigDecimal.valueOf(averageRating) : BigDecimal.ZERO);
        serviceMapper.save(service);
    }
}
