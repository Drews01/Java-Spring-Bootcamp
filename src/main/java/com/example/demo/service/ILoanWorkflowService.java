package com.example.demo.service;

import com.example.demo.dto.LoanActionRequest;
import com.example.demo.dto.LoanApplicationDTO;
import com.example.demo.dto.LoanSubmitRequest;
import java.util.List;

/**
 * Loan Workflow Service Interface.
 *
 * <p>Defines the contract for loan workflow operations including:
 *
 * <ul>
 *   <li>Loan submission
 *   <li>Loan action processing (approve, reject, etc.)
 *   <li>Loan listing by status
 * </ul>
 *
 * @author Java Spring Bootcamp
 * @version 1.0
 */
public interface ILoanWorkflowService {

  /**
   * Submits a new loan application.
   *
   * @param request the loan submission request
   * @param userId the ID of the user submitting the loan
   * @return LoanApplicationDTO containing the submitted loan information
   */
  LoanApplicationDTO submitLoan(LoanSubmitRequest request, Long userId);

  /**
   * Performs an action on a loan application.
   *
   * @param request the loan action request
   * @param actorUserId the ID of the user performing the action
   * @return LoanApplicationDTO containing the updated loan information
   */
  LoanApplicationDTO performAction(LoanActionRequest request, Long actorUserId);

  /**
   * Gets allowed actions for a loan in a specific status for a user.
   *
   * @param currentStatus the current loan status
   * @param userId the user ID
   * @return list of allowed action names
   */
  List<String> getAllowedActions(String currentStatus, Long userId);
}
