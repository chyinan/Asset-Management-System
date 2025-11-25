package com.project.asset.repository;

import com.project.asset.domain.entity.LoanReminderSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanReminderSettingRepository extends JpaRepository<LoanReminderSetting, Long> {}

