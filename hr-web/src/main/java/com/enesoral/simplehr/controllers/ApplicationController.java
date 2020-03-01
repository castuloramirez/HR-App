package com.enesoral.simplehr.controllers;

import com.enesoral.simplehr.models.Application;
import com.enesoral.simplehr.models.Job;
import com.enesoral.simplehr.models.User;
import com.enesoral.simplehr.services.ApplicationService;
import com.enesoral.simplehr.services.JobService;
import com.enesoral.simplehr.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JobService jobService;
    private final UserService userService;

    public ApplicationController(ApplicationService applicationService, JobService jobService, UserService userService) {
        this.applicationService = applicationService;
        this.jobService = jobService;
        this.userService = userService;
    }

    @GetMapping({"/", "/index"})
    public String listApplications(Model model) {
        model.addAttribute("applications", applicationService.findAll());
        return "applications/index";
    }

    @PostMapping("/{id}/detail")
    public String getApplicationById(@PathVariable String id, Model model){
        model.addAttribute("app", applicationService.findById(Long.parseLong(id)));
        return "applications/detail";
    }

    @GetMapping("/{id}/applyform")
    public String showApplyForm(@PathVariable String id, Model model) {
        Job job = jobService.findById(Long.parseLong(id));
        if (isAlreadyApplied(job, userService.getLoggedUser())) {
            return "redirect:/jobs/index?alreadyapplied";
        }

        if (userService.getLoggedUser().getResumeDirectory() == null) {
            return "redirect:/users/resumeform?haveto";
        }

        model.addAttribute("jobId", Long.parseLong(id));
        return "applications/apply-form";
    }

    @PostMapping("/{id}/apply")
    public String applyJob(@PathVariable String id, @RequestParam("thoughts") String thoughts) {

        Application application = new Application(LocalDateTime.now(), userService.getLoggedUser(),
                jobService.findById(Long.parseLong(id)), thoughts);

        applicationService.save(application);
        return "redirect:/jobs/index?applysuccess";
    }

    @PostMapping("/{id}/delete")
    public String deleteApplication(@PathVariable String id) {
        applicationService.deleteById(Long.parseLong(id));
        return "redirect:/applications/index?deleted";
    }

    private boolean isAlreadyApplied(Job job, User user) {
        return applicationService.isAlreadyApplied(job, user);
    }
}
