package com.skfkfkvlrm.adminservice.domain.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketSettingsRepository extends JpaRepository<MarketSettings, Integer> {
}
