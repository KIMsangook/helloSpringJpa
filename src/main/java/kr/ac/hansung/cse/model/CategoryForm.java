package kr.ac.hansung.cse.model;

@Getter @Setter
public class CategoryForm {
    @NotBlank(message = "카테고리 이름을 입력하세요")
    @Size(max = 50, message = "50자 이내로 입력하세요")
    private String name; }

