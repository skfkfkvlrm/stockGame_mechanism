package com.skfkfkvlrm.pointservice.domain.point;

import com.skfkfkvlrm.pointservice.domain.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/points")
@RequiredArgsConstructor
public class PointInternalController {

    private final MyAssetRepository myAssetRepository;

    @GetMapping("/{studentId}")
    public ApiResponse<Integer> getStudentPoint(@PathVariable("studentId") String studentId) {
        Integer point = myAssetRepository.getStudentPoint(studentId);
        return ApiResponse.success("Point fetched", point != null ? point : 0);
    }

    @PostMapping("/{studentId}/decrease")
    public ApiResponse<Boolean> decreasePoint(@PathVariable("studentId") String studentId, @RequestParam("amount") int amount) {
        myAssetRepository.setStudentPointDown(amount, studentId);
        return ApiResponse.success("Point decreased", true);
    }

    @PostMapping("/{studentId}/increase")
    public ApiResponse<Boolean> increasePoint(@PathVariable("studentId") String studentId, @RequestParam("amount") int amount) {
        myAssetRepository.setStudentPointUp(amount, studentId);
        return ApiResponse.success("Point increased", true);
    }
}
