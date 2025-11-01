package lt.elektromeistras.controller;

import lt.elektromeistras.domain.GLAccount;
import lt.elektromeistras.dto.request.CreateGLAccountRequest;
import lt.elektromeistras.repository.GLAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gl-accounts")
@RequiredArgsConstructor
@Slf4j
public class GLAccountController {

    private final GLAccountRepository glAccountRepository;

    /**
     * Get all GL accounts
     * GET /api/gl-accounts
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<GLAccount>> getAllAccounts() {
        List<GLAccount> accounts = glAccountRepository.findAll();
        return ResponseEntity.ok(accounts);
    }

    /**
     * Get active GL accounts
     * GET /api/gl-accounts/active
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<GLAccount>> getActiveAccounts() {
        List<GLAccount> accounts = glAccountRepository.findByIsActiveTrue();
        return ResponseEntity.ok(accounts);
    }

    /**
     * Get GL account by ID
     * GET /api/gl-accounts/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<GLAccount> getById(@PathVariable UUID id) {
        GLAccount account = glAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GL Account not found with id: " + id));
        return ResponseEntity.ok(account);
    }

    /**
     * Get GL account by code
     * GET /api/gl-accounts/code/{code}
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<GLAccount> getByCode(@PathVariable String code) {
        GLAccount account = glAccountRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("GL Account not found with code: " + code));
        return ResponseEntity.ok(account);
    }

    /**
     * Get GL accounts by type
     * GET /api/gl-accounts/type/{accountType}
     */
    @GetMapping("/type/{accountType}")
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<GLAccount>> getByAccountType(@PathVariable String accountType) {
        GLAccount.AccountType type = GLAccount.AccountType.valueOf(accountType);
        List<GLAccount> accounts = glAccountRepository.findByAccountType(type);
        return ResponseEntity.ok(accounts);
    }

    /**
     * Get GL accounts by category
     * GET /api/gl-accounts/category/{accountCategory}
     */
    @GetMapping("/category/{accountCategory}")
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<GLAccount>> getByAccountCategory(@PathVariable String accountCategory) {
        GLAccount.AccountCategory category = GLAccount.AccountCategory.valueOf(accountCategory);
        List<GLAccount> accounts = glAccountRepository.findByAccountCategory(category);
        return ResponseEntity.ok(accounts);
    }

    /**
     * Get root level GL accounts (no parent)
     * GET /api/gl-accounts/root
     */
    @GetMapping("/root")
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<GLAccount>> getRootAccounts() {
        List<GLAccount> accounts = glAccountRepository.findByParentAccountIsNull();
        return ResponseEntity.ok(accounts);
    }

    /**
     * Get child GL accounts for a parent
     * GET /api/gl-accounts/{parentId}/children
     */
    @GetMapping("/{parentId}/children")
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<GLAccount>> getChildAccounts(@PathVariable UUID parentId) {
        GLAccount parentAccount = glAccountRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent GL Account not found"));
        List<GLAccount> accounts = glAccountRepository.findByParentAccount(parentAccount);
        return ResponseEntity.ok(accounts);
    }

    /**
     * Create new GL account
     * POST /api/gl-accounts
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('GL_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<GLAccount> createAccount(@Valid @RequestBody CreateGLAccountRequest request) {
        log.info("Creating new GL account: {}", request.getCode());

        GLAccount account = GLAccount.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .accountType(GLAccount.AccountType.valueOf(request.getAccountType()))
                .accountCategory(GLAccount.AccountCategory.valueOf(request.getAccountCategory()))
                .normalBalance(GLAccount.NormalBalance.valueOf(request.getNormalBalance()))
                .allowDirectPosting(request.getAllowDirectPosting())
                .requireDepartment(request.getRequireDepartment())
                .requireCostCenter(request.getRequireCostCenter())
                .requireBusinessObject(request.getRequireBusinessObject())
                .sortOrder(request.getSortOrder())
                .isActive(true)
                .build();

        if (request.getParentAccountId() != null) {
            GLAccount parentAccount = glAccountRepository.findById(request.getParentAccountId())
                    .orElseThrow(() -> new RuntimeException("Parent GL Account not found"));
            account.setParentAccount(parentAccount);
        }

        GLAccount createdAccount = glAccountRepository.save(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    /**
     * Update GL account
     * PUT /api/gl-accounts/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('GL_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<GLAccount> updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody CreateGLAccountRequest request) {
        log.info("Updating GL account: {}", id);

        GLAccount account = glAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GL Account not found"));

        account.setName(request.getName());
        account.setDescription(request.getDescription());
        account.setAccountType(GLAccount.AccountType.valueOf(request.getAccountType()));
        account.setAccountCategory(GLAccount.AccountCategory.valueOf(request.getAccountCategory()));
        account.setNormalBalance(GLAccount.NormalBalance.valueOf(request.getNormalBalance()));
        account.setAllowDirectPosting(request.getAllowDirectPosting());
        account.setRequireDepartment(request.getRequireDepartment());
        account.setRequireCostCenter(request.getRequireCostCenter());
        account.setRequireBusinessObject(request.getRequireBusinessObject());
        account.setSortOrder(request.getSortOrder());

        GLAccount updatedAccount = glAccountRepository.save(account);
        return ResponseEntity.ok(updatedAccount);
    }

    /**
     * Deactivate GL account
     * POST /api/gl-accounts/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyAuthority('GL_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<GLAccount> deactivateAccount(@PathVariable UUID id) {
        log.info("Deactivating GL account: {}", id);

        GLAccount account = glAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GL Account not found"));

        account.setIsActive(false);
        GLAccount updatedAccount = glAccountRepository.save(account);

        return ResponseEntity.ok(updatedAccount);
    }

    /**
     * Activate GL account
     * POST /api/gl-accounts/{id}/activate
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('GL_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<GLAccount> activateAccount(@PathVariable UUID id) {
        log.info("Activating GL account: {}", id);

        GLAccount account = glAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GL Account not found"));

        account.setIsActive(true);
        GLAccount updatedAccount = glAccountRepository.save(account);

        return ResponseEntity.ok(updatedAccount);
    }
}
