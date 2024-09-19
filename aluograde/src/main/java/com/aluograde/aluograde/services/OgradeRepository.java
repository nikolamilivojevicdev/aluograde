package com.aluograde.aluograde.services;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aluograde.aluograde.models.AluOgrade;



public interface OgradeRepository extends JpaRepository<AluOgrade,Integer> {
	  List<AluOgrade> findByNameContaining(String name, Sort sort);
}
