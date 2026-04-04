package com.Zorvyn.Finance_Dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.Zorvyn.Finance_Dashboard.model.AppUser;
import com.Zorvyn.Finance_Dashboard.model.FinancialRecord;
import com.Zorvyn.Finance_Dashboard.model.RecordType;
import com.Zorvyn.Finance_Dashboard.model.Role;
import com.Zorvyn.Finance_Dashboard.model.UserStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ModelTests {

    @Test
    void appUserStoresMultipleRolesAndStatus() {
        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");

        Role analystRole = new Role();
        analystRole.setId(2L);
        analystRole.setName("ANALYST");

        AppUser user = new AppUser();
        user.setId(10L);
        user.setName("Admin Analyst");
        user.setUsername("admin-analyst");
        user.setEmail("admin-analyst@finance.local");
        user.setPassword("encoded-password");
        user.setRoles(Set.of(adminRole, analystRole));
        user.setStatus(UserStatus.ACTIVE);

        assertEquals(10L, user.getId());
        assertEquals("admin-analyst", user.getUsername());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getName())));
    }

    @Test
    void financialRecordLinksToCreatorAndPreservesValues() {
        AppUser creator = new AppUser();
        creator.setId(1L);
        creator.setName("Admin User");

        FinancialRecord record = new FinancialRecord();
        record.setId(20L);
        record.setAmount(new BigDecimal("199.50"));
        record.setType(RecordType.EXPENSE);
        record.setCategory("Travel");
        record.setDate(LocalDate.of(2026, 4, 4));
        record.setNotes("Flight transfer");
        record.setCreatedBy(creator);

        assertEquals(20L, record.getId());
        assertEquals(new BigDecimal("199.50"), record.getAmount());
        assertEquals(RecordType.EXPENSE, record.getType());
        assertEquals("Travel", record.getCategory());
        assertSame(creator, record.getCreatedBy());
    }

    @Test
    void roleEntityStoresIdAndName() {
        Role role = new Role();
        role.setId(99L);
        role.setName("VIEWER");

        assertEquals(99L, role.getId());
        assertEquals("VIEWER", role.getName());
    }
}
