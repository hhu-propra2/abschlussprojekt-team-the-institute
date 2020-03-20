package mops.portfolios.controller;


import lombok.AllArgsConstructor;
import mops.portfolios.AccountService;
import mops.portfolios.domain.group.Group;
import mops.portfolios.domain.portfolio.Portfolio;
import mops.portfolios.domain.portfolio.PortfolioService;
import mops.portfolios.domain.portfolio.templates.Template;
import mops.portfolios.domain.portfolio.templates.TemplateService;
import mops.portfolios.domain.user.UserService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@AllArgsConstructor
public class StudentController {

  @Autowired
  private transient final AccountService accountService;

  @Autowired
  private transient UserService userService;

  @Autowired
  private transient PortfolioService portfolioService;
  @Autowired
  private transient TemplateService templateService;

  /**
   * Index mapping for GET requests.
   *
   * @param model The Spring Model to add the attributes to
   * @return The page to load
   */
  @SuppressWarnings("PMD")
  @GetMapping("/")
  @RolesAllowed({"ROLE_studentin"})
  public String index(Model model, KeycloakAuthenticationToken token) {
    accountService.authorize(model, token);

    return "redirect:/list";
  }

  /**
   * List mapping for GET requests.
   *
   * @param model The Spring Model to add the attributes to
   * @return The page to load
   */
  @SuppressWarnings("PMD")
  @GetMapping("/list")
  @RolesAllowed({"ROLE_studentin"})
  public String listPortfolios(Model model, KeycloakAuthenticationToken token) {
    accountService.authorize(model, token);

    String userName = accountService.getUserName(token);

    List<Group> groups = userService.getGroupsByUserName(userName);
    // TODO Implement optional sublisting with method overload in portfolioService
    List<Portfolio> groupPortfolios = portfolioService.findAllByGroupList(groups);
    List<Portfolio> userPortfolios = portfolioService.findAllByUserId(userName);
    List<Portfolio> allPortfolios = Stream.of(userPortfolios, groupPortfolios).flatMap(Collection::stream).collect(Collectors.toList());

    model.addAttribute("groupPortfolios", groupPortfolios);
    model.addAttribute("userPortfolios", userPortfolios);
    model.addAttribute("allPortfolios", allPortfolios);

    return "student/list";
  }

  /**
   * View mapping for GET requests.
   *
   * @param model       The spring model to add the attributes to
   * @param portfolioId The ID of the portfolio
   * @return The page to load
   */
  @SuppressWarnings("PMD")
  @GetMapping("/view")
  @RolesAllowed({"ROLE_studentin"})
  public String viewPortfolio(Model model, @RequestParam Long portfolioId, KeycloakAuthenticationToken token) {
    accountService.authorize(model, token);

    Portfolio portfolio = portfolioService.findPortfolioById(portfolioId);

    model.addAttribute("portfolio", portfolio);

    return "student/view";
  }

  /**
   * Create mapping for GET requests.
   *
   * @param model The spring model to add the attributes to
   * @return The page to load
   */
  @SuppressWarnings("PMD")
  @GetMapping("/create")
  @RolesAllowed({"ROLE_studentin"})
  public String createPortfolio(Model model, KeycloakAuthenticationToken token) {
    accountService.authorize(model, token);

    List<Template> templateList = templateService.getAll();

    model.addAttribute("templateList", templateList);

    return "student/create";
  }

  /**
   * Submit mapping for GET requests.
   *
   * @param model       The spring model to add the attributes to
   * @param portfolioId The portfolio id
   * @return The page to load
   */
  @GetMapping("/submit")
  @RolesAllowed({"ROLE_studentin"})
  public String submitPortfolio(Model model, /*@RequestParam Long portfolioId,*/ KeycloakAuthenticationToken token) {
    accountService.authorize(model, token);

    //Template template = templateService.getById(portfolioId);
    Template template = templateService.getByTitle("Propra2");

    model.addAttribute("template", template);

    return "student/submit";
  }
}