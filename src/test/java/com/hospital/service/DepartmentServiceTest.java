package com.hospital.service;

import com.hospital.dto.DepartmentDTO;
import com.hospital.entity.Department;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DepartmentRepository;
import com.hospital.util.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentService - Unit Tests")
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department;

    @BeforeEach
    void setUp() {
        department = TestDataBuilder.buildDepartment();
        department.setId(1L);
    }

    // ── getAllDepartments ─────────────────────────────────────────
    @Nested
    @DisplayName("getAllDepartments()")
    class GetAllDepartments {

        @Test
        @DisplayName("✅ يرجع قائمة بجميع الأقسام")
        void shouldReturnAllDepartments() {
            given(departmentRepository.findAll()).willReturn(List.of(department));

            List<DepartmentDTO.Response> result = departmentService.getAllDepartments();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("قسم القلب");
        }

        @Test
        @DisplayName("✅ يرجع قائمة فارغة إذا لم يكن هناك أقسام")
        void shouldReturnEmptyListWhenNoDepartments() {
            given(departmentRepository.findAll()).willReturn(List.of());

            List<DepartmentDTO.Response> result = departmentService.getAllDepartments();

            assertThat(result).isEmpty();
        }
    }

    // ── getDepartmentById ─────────────────────────────────────────
    @Nested
    @DisplayName("getDepartmentById()")
    class GetDepartmentById {

        @Test
        @DisplayName("✅ يرجع القسم عند وجود المعرف")
        void shouldReturnDepartmentWhenFound() {
            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));

            DepartmentDTO.Response result = departmentService.getDepartmentById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("قسم القلب");
            assertThat(result.getStatus()).isEqualTo(Department.DepartmentStatus.ACTIVE);
        }

        @Test
        @DisplayName("❌ يرمي ResourceNotFoundException عند عدم وجود القسم")
        void shouldThrowExceptionWhenNotFound() {
            given(departmentRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.getDepartmentById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ── createDepartment ──────────────────────────────────────────
    @Nested
    @DisplayName("createDepartment()")
    class CreateDepartment {

        @Test
        @DisplayName("✅ ينشئ قسم جديد بنجاح")
        void shouldCreateDepartmentSuccessfully() {
            DepartmentDTO.Request request = new DepartmentDTO.Request();
            request.setName("قسم الأعصاب");
            request.setDescription("علاج الأعصاب");
            request.setStatus(Department.DepartmentStatus.ACTIVE);

            Department savedDept = TestDataBuilder.buildDepartment("قسم الأعصاب");
            savedDept.setId(2L);

            given(departmentRepository.existsByName("قسم الأعصاب")).willReturn(false);
            given(departmentRepository.save(any(Department.class))).willReturn(savedDept);

            DepartmentDTO.Response result = departmentService.createDepartment(request);

            assertThat(result.getName()).isEqualTo("قسم الأعصاب");
            then(departmentRepository).should().save(any(Department.class));
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند تكرار اسم القسم")
        void shouldThrowExceptionWhenNameAlreadyExists() {
            DepartmentDTO.Request request = new DepartmentDTO.Request();
            request.setName("قسم القلب");
            request.setStatus(Department.DepartmentStatus.ACTIVE);

            given(departmentRepository.existsByName("قسم القلب")).willReturn(true);

            assertThatThrownBy(() -> departmentService.createDepartment(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("قسم القلب");

            then(departmentRepository).should(never()).save(any());
        }
    }

    // ── updateDepartment ──────────────────────────────────────────
    @Nested
    @DisplayName("updateDepartment()")
    class UpdateDepartment {

        @Test
        @DisplayName("✅ يعدل القسم بنجاح")
        void shouldUpdateDepartmentSuccessfully() {
            DepartmentDTO.Request request = new DepartmentDTO.Request();
            request.setName("قسم القلب");
            request.setDescription("وصف محدث");
            request.setLocation("الطابق الثالث");
            request.setStatus(Department.DepartmentStatus.ACTIVE);

            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
            given(departmentRepository.existsByName(any())).willReturn(false);
            given(departmentRepository.save(any())).willReturn(department);

            DepartmentDTO.Response result = departmentService.updateDepartment(1L, request);

            assertThat(result).isNotNull();
            then(departmentRepository).should().save(any(Department.class));
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند تعديل قسم غير موجود")
        void shouldThrowExceptionWhenUpdatingNonExistentDepartment() {
            DepartmentDTO.Request request = new DepartmentDTO.Request();
            request.setName("أي قسم");
            request.setStatus(Department.DepartmentStatus.ACTIVE);

            given(departmentRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.updateDepartment(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── deleteDepartment ──────────────────────────────────────────
    @Nested
    @DisplayName("deleteDepartment()")
    class DeleteDepartment {

        @Test
        @DisplayName("✅ يحذف القسم الفارغ بنجاح")
        void shouldDeleteEmptyDepartmentSuccessfully() {
            department.setDoctors(List.of());
            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));

            assertThatCode(() -> departmentService.deleteDepartment(1L)).doesNotThrowAnyException();
            then(departmentRepository).should().delete(department);
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند حذف قسم يحتوي على أطباء")
        void shouldThrowExceptionWhenDepartmentHasDoctors() {
            department.setDoctors(List.of(TestDataBuilder.buildDoctor(department)));
            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));

            assertThatThrownBy(() -> departmentService.deleteDepartment(1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("أطباء");

            then(departmentRepository).should(never()).delete(any());
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند حذف قسم غير موجود")
        void shouldThrowExceptionWhenDeletingNonExistentDepartment() {
            given(departmentRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.deleteDepartment(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── getActiveDepartments ──────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع الأقسام النشطة فقط")
    void shouldReturnOnlyActiveDepartments() {
        given(departmentRepository.findByStatus(Department.DepartmentStatus.ACTIVE))
                .willReturn(List.of(department));

        List<DepartmentDTO.Response> result = departmentService.getActiveDepartments();

        assertThat(result).allMatch(d -> d.getStatus() == Department.DepartmentStatus.ACTIVE);
    }
}
