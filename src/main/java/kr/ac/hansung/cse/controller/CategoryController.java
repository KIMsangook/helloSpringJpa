package kr.ac.hansung.cse.controller;


import jakarta.validation.Valid;
import kr.ac.hansung.cse.model.Category;
import kr.ac.hansung.cse.model.CategoryForm;
import kr.ac.hansung.cse.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {


    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    // ─────────────────────────────────────────────────────────────────
    // GET /categories - 카테고리 목록 조회
    // ─────────────────────────────────────────────────────────────────

    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategory();
        model.addAttribute("categories", categories);
        return "categoryList";
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /categories/create - 상품 등록 폼 표시
    // ─────────────────────────────────────────────────────────────────

    /**
     * 빈 CategoryForm 객체를 Model에 담아 폼을 표시합니다.
     *
     * [CategoryForm DTO를 사용하는 이유]
     * 1. Bean Validation 어노테이션을 엔티티가 아닌 DTO에 적용합니다.
     * 2. JPA 엔티티의 보호 생성자 문제를 우회합니다.
     * 3. 외부에서 수정 불가한 필드(id 등)를 폼에서 분리합니다.
     *
     * Model attribute 이름: "CategoryForm"
     *   → Thymeleaf에서 th:object="${CategoryForm}"으로 접근합니다.
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("CategoryForm", new CategoryForm());
        return "CategoryForm";
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /categories/create - 카테고리 등록 처리
    // ─────────────────────────────────────────────────────────────────

    /**
     * @Valid: CategoryForm 선언된 Bean Validation 어노테이션을 실행합니다.
     *         (@NotBlank, @NotNull, @DecimalMin 등)
     *
     * @ModelAttribute("CategoryForm") CategoryForm categoryForm:
     *   - HTTP POST 요청 파라미터를 ProductForm 객체에 자동 바인딩합니다.
     *   - "CategoryForm" 이름으로 Model에 자동 등록됩니다.
     *   - @Valid에 의해 검증이 수행됩니다.
     *
     * BindingResult bindingResult:
     *   - 검증 결과(오류 목록)를 담는 객체입니다.
     *   - 반드시 @ModelAttribute 파라미터 바로 다음에 위치해야 합니다.
     *   - BindingResult가 없으면 검증 실패 시 MethodArgumentNotValidException 발생
     *   - BindingResult가 있으면 오류를 직접 처리할 수 있습니다.
     *
     * [처리 흐름]
     * ① Spring MVC가 폼 파라미터 → CategoryForm 바인딩
     * ② @Valid에 의해 Bean Validation 실행
     * ③ bindingResult.hasErrors()로 오류 확인
     *   - 오류 있음 → 폼 뷰로 돌아감 (오류 메시지 표시)
     *   - 오류 없음 → 서비스 호출 → 리다이렉트 (PRG 패턴)
     */
    @PostMapping("/create")
    public String createCategory(@Valid @ModelAttribute("CategoryForm") CategoryForm categoryForm,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        // 검증 오류가 있으면 폼을 다시 표시합니다.
        // bindingResult는 CategoryForm 함께 Model에 자동으로 포함되므로
        // Thymeleaf에서 th:errors로 오류 메시지에 접근할 수 있습니다.
        if (bindingResult.hasErrors()) {
            return "CategoryForm"; // 오류가 있는 채로 폼 뷰 재표시
        }

        //검증 완료시 등록
        try {
            categoryService.createCategory(categoryForm.getName());
            redirectAttributes.addFlashAttribute("successMessage", "등록 완료");
        } catch (DuplicateCategoryException e) {
            // 중복 예외
            bindingResult.rejectValue("name", "duplicate", e.getMessage());
            return "categoryForm"; }
        return "redirect:/categories";

    }

    // ─────────────────────────────────────────────────────────────────
    // POST /categories/{id}/delete - 카테고리 삭제 처리
    // ─────────────────────────────────────────────────────────────────

    /**
     * HTML 폼은 GET/POST만 지원하므로 DELETE 대신 POST를 사용합니다.
     * (REST API에서는 HTTP DELETE 메서드를 사용하는 것이 표준입니다.)
     *
     * 삭제 전 카테고리 조회 후 연결 상품이 있는지 확인합니다
     * 삭제 후에는 상세 페이지로 돌아갈 수 없으므로 목록으로 리다이렉트합니다.
     */
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
    try{
        categoryService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("successMessage", "삭제 완료");
    } catch (IllegalStateException e) {
        // 연결된 상품 있는 경우
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }  return "redirect:/categories";

}

}
