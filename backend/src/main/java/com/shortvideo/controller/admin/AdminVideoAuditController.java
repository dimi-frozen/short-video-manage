package com.shortvideo.controller.admin;

import com.shortvideo.dto.admin.PendingVideoVO;
import com.shortvideo.service.AdminVideoAuditService;
import com.shortvideo.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/admin/video", "/api/admin/video"})
public class AdminVideoAuditController {

    private final AdminVideoAuditService adminVideoAuditService;

    public AdminVideoAuditController(AdminVideoAuditService adminVideoAuditService) {
        this.adminVideoAuditService = adminVideoAuditService;
    }

    @GetMapping("/pending")
    public Result<List<PendingVideoVO>> pending(@RequestParam(value = "limit", required = false) Integer limit) {
        return Result.success(adminVideoAuditService.listPending(limit == null ? 100 : limit));
    }

    @PostMapping("/approve")
    public Result<Void> approve(@RequestParam("id") Long id) {
        adminVideoAuditService.approve(id);
        return Result.success("审核通过");
    }

    @PostMapping("/reject")
    public Result<Void> reject(@RequestParam("id") Long id) {
        adminVideoAuditService.reject(id);
        return Result.success("审核拒绝");
    }


}

