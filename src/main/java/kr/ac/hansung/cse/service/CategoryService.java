package kr.ac.hansung.cse.service;


import kr.ac.hansung.cse.exception.DuplicateCategoryException;
import kr.ac.hansung.cse.model.Category;
import kr.ac.hansung.cse.model.Product;
import kr.ac.hansung.cse.repository.CategoryRepository;
import kr.ac.hansung.cse.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true) // 클래스 기본값: 읽기 전용 트랜잭션
public class CategoryService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public CategoryService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }


    /**
     * 모든 카테고리 조회
     * readOnly = true (클래스 레벨 설정 상속): 읽기 전용 트랜잭션
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll(); }




    /**
     * 새 카테고리 등록
     *
     * @Transactional: readOnly 기본값을 false로 오버라이드합니다.
     *                 쓰기 작업에는 반드시 readOnly = false가 필요합니다.
     *                 DB 변경 작업이 포함되므로 트랜잭션이 필수입니다.
     *
     */
    @Transactional // readOnly = false (쓰기 가능)
    public Category createCategory(String name) {
        // 비즈니스 유효성 검사 예시
        categoryRepository.findByName(name)
                .ifPresent(c -> { throw new DuplicateCategoryException(name); });
        return categoryRepository.save(new Category(name));

    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteCategory(Long id) {
        if(categoryRepository.countProductsByCategoryId(id) != 0){
            throw new IllegalStateException(
                    "상품이 연결되어 있어 삭제할 수 없습니다.");
        }
        categoryRepository.delete(id);
    }
}
