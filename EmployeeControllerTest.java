/*
 * Copyright (c) 2008-2020
 * LANIT
 * All rights reserved.
 *
 * This product and related documentation are protected by copyright and
 * distributed under licenses restricting its use, copying, distribution, and
 * decompilation. No part of this product or related documentation may be
 * reproduced in any form by any means without prior written authorization of
 * LANIT and its licensors, if any.
 *
 * $
 */
package ru.lanit.bpm.jedu.hrjedi.rest.form;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import ru.lanit.bpm.jedu.hrjedi.model.Employee;
import ru.lanit.bpm.jedu.hrjedi.rest.EmployeeController;
import ru.lanit.bpm.jedu.hrjedi.service.EmployeeService;
import ru.lanit.bpm.jedu.hrjedi.service.SecurityService;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

@RunWith(EasyMockRunner.class)
public class EmployeeControllerTest {
    @TestSubject
    EmployeeController employeeController;
    @Mock
    SecurityService securityServiceMock;
    @Mock
    EmployeeService employeeServiceMock;
    @Mock
    Employee employeeMock;

    @Before
    public void setUp() {
        employeeController = new EmployeeController(securityServiceMock, employeeServiceMock);
    }

    @Test
    public void updateEmailWithValidEmailValue() {
        String email = "01234567890.testTESTтестТЕСТёЁ-@01234567890.testTESTтестТЕСТёЁ-";
        prepareAndReplayMocks(email);
        ResponseEntity<String> result = employeeController.updateEmail(email);
        assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    public void updateEmailWithEmptyEmailValue() {
        String email = "";
        prepareAndReplayMocks(email);
        ResponseEntity<String> result = employeeController.updateEmail(email);
        assertEquals(400, result.getStatusCodeValue());
    }

    @Test
    public void updateEmailWithInvalidEmailValue() {
        String email = "!#$%ˆˆ*()@@!@#$%ˆˆ*()";
        prepareAndReplayMocks(email);
        ResponseEntity<String> result = employeeController.updateEmail(email);
        assertEquals(400, result.getStatusCodeValue());
    }

    private void prepareAndReplayMocks(String email) {
        expect(securityServiceMock.getCurrentEmployee()).andReturn(employeeMock);
        employeeMock.setEmail(email);
        expectLastCall();
        expect(employeeServiceMock.save(employeeMock)).andReturn(employeeMock);
        replay(securityServiceMock, employeeMock, employeeServiceMock);
    }
}
