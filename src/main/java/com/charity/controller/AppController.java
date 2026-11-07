package com.charity.controller;

import com.charity.dao.AidRequestDAO;
import com.charity.dao.BeneficiaryDAO;
import com.charity.dao.DistributionDAO;
import com.charity.dao.DonationDAO;
import com.charity.dao.DonorDAO;
import com.charity.dao.OracleAidRequestDAO;
import com.charity.dao.OracleBeneficiaryDAO;
import com.charity.dao.OracleDistributionDAO;
import com.charity.dao.OracleDonationDAO;
import com.charity.dao.OracleDonorDAO;
import com.charity.dao.OracleUserDAO;
import com.charity.dao.UserDAO;
import com.charity.model.User;
import com.charity.service.AuthenticationService;
import com.charity.service.BeneficiaryService;
import com.charity.service.DistributionService;
import com.charity.service.DonationService;
import com.charity.service.DonorService;
import com.charity.service.FinancialService;
import com.charity.view.DashboardView;
import com.charity.view.DistributionView;
import com.charity.view.DonorView;
import com.charity.view.LoginView;
import com.charity.view.MainFrame;
import com.charity.view.ReportView;
import com.charity.view.AidRequestView;
import com.charity.view.BeneficiaryView;
import com.charity.view.DonationView;

/**
 * Central application controller. Holds services/DAOs, manages the current user,
 * and coordinates navigation between views.
 */
public class AppController {
    public static final AppController INSTANCE = new AppController();

    // DAOs
    private final UserDAO userDAO = new OracleUserDAO();
    private final DonorDAO donorDAO = new OracleDonorDAO();
    private final BeneficiaryDAO beneficiaryDAO = new OracleBeneficiaryDAO();
    private final DonationDAO donationDAO = new OracleDonationDAO();
    private final AidRequestDAO requestDAO = new OracleAidRequestDAO();
    private final DistributionDAO distributionDAO = new OracleDistributionDAO();

    // Services
    public final AuthenticationService authService = new AuthenticationService(userDAO);
    public final DonorService donorService = new DonorService(donorDAO);
    public final BeneficiaryService beneficiaryService = new BeneficiaryService(beneficiaryDAO);
    public final DonationService donationService = new DonationService(donationDAO, donorDAO);
    public final AidRequestService requestService = new AidRequestService(requestDAO, beneficiaryDAO);
    public final DistributionService distributionService =
            new DistributionService(requestDAO, distributionDAO, donationDAO);
    public final FinancialService financialService = new FinancialService(donationDAO, distributionDAO);

    private MainFrame mainFrame;
    private User currentUser;

    // Cached views (reused across navigation)
    private DashboardView dashboardView;
    private DonorView donorView;
    private DonationView donationView;
    private BeneficiaryView beneficiaryView;
    private AidRequestView aidRequestView;
    private DistributionView distributionView;
    private ReportView reportView;

    private AppController() {
    }

    public void init(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Attempts login. On success, shows the administrator dashboard.
     */
    public boolean login(String username, String password) {
        User user = authService.authenticate(username, password);
        if (user == null) {
            return false;
        }
        currentUser = user;
        mainFrame.setUser(user);
        showDashboard();
        return true;
    }

    public void logout() {
        currentUser = null;
        mainFrame.setUser(null);
        mainFrame.showView("login", null);
    }

    public void requireAdmin() {
        if (currentUser == null || !authService.isAdmin(currentUser)) {
            throw new SecurityException("Administrator access required");
        }
    }

    // ---- Navigation ----
    public void showDashboard() {
        requireAdmin();
        if (dashboardView == null) dashboardView = new DashboardView(this);
        mainFrame.showView("dashboard", dashboardView);
    }

    public void showDonors() {
        requireAdmin();
        if (donorView == null) donorView = new DonorView(this);
        mainFrame.showView("donors", donorView);
    }

    public void showDonations() {
        requireAdmin();
        if (donationView == null) donationView = new DonationView(this);
        mainFrame.showView("donations", donationView);
    }

    public void showBeneficiaries() {
        requireAdmin();
        if (beneficiaryView == null) beneficiaryView = new BeneficiaryView(this);
        mainFrame.showView("beneficiaries", beneficiaryView);
    }

    public void showAidRequests() {
        requireAdmin();
        if (aidRequestView == null) aidRequestView = new AidRequestView(this);
        mainFrame.showView("aidrequests", aidRequestView);
    }

    public void showDistributions() {
        requireAdmin();
        if (distributionView == null) distributionView = new DistributionView(this);
        mainFrame.showView("distributions", distributionView);
    }

    public void showReports() {
        requireAdmin();
        if (reportView == null) reportView = new ReportView(this);
        mainFrame.showView("reports", reportView);
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    // DAO access for views (read-only reporting)
    public DonationDAO getDonationDAO() { return donationDAO; }
    public DistributionDAO getDistributionDAO() { return distributionDAO; }
    public AidRequestDAO getRequestDAO() { return requestDAO; }
    public DonorDAO getDonorDAO() { return donorDAO; }
    public BeneficiaryDAO getBeneficiaryDAO() { return beneficiaryDAO; }
}

