package org.mbruncic.cookingplanner.data.service;

import org.mbruncic.cookingplanner.data.entity.Person;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface PersonRepository extends JpaRepository<Person, Integer> {

}