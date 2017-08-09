/**
 * 
 */
package com.anthem.oss.nimbus.test.sample.um.model;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import com.anthem.oss.nimbus.core.domain.definition.Domain;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * @author Soham Chakravarti
 *
 */
@Data
@Domain("umcase") 
public class UMCaseSearchCriteria {

	@NotNull
	private String caseId;

	private String firstName;

	private String lastName;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") 
	private LocalDate dob;
	
}
