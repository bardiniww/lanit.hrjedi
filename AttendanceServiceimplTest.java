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
package ru.lanit.bpm.jedu.hrjedi.service.impl;

import org.easymock.IExpectationSetters;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsBlockJUnit4ClassRunner;
import org.unitils.easymock.annotation.Mock;
import ru.lanit.bpm.jedu.hrjedi.repository.AttendanceRepository;
import ru.lanit.bpm.jedu.hrjedi.service.DateTimeService;

import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static java.time.Month.*;
import static java.time.YearMonth.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.SetUtils.hashSet;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.unitils.easymock.EasyMockUnitils.replay;

@RunWith(UnitilsBlockJUnit4ClassRunner.class)
public class AttendanceServiceimplTest {
    public static final int YEAR = 2020;
    public static final int YEAR_NEXT = YEAR + 1;
    public static final YearMonth YM_ENTRY_FEBRUARY = YearMonth.of(YEAR, FEBRUARY);
    public static final YearMonth YM_ENTRY_APRIL = YearMonth.of(YEAR, APRIL);
    public static final YearMonth YM_ENTRY_JUNE = YearMonth.of(YEAR, JUNE);
    public static final YearMonth YM_ENTRY_OCTOBER = YearMonth.of(YEAR, OCTOBER);
    public static final YearMonth YM_ENTRY_DECEMBER = YearMonth.of(YEAR, DECEMBER);

    @TestSubject
    AttendanceServiceimpl attendanceService = new AttendanceServiceimpl();

    @Mock
    AttendanceRepository attendanceRepository;
    @Mock
    DateTimeService dateTimeService;

    @Before
    public void setUp() {
        attendanceService.setAttendanceRepository(attendanceRepository);
        attendanceService.setDateTimeService(dateTimeService);
    }

    @Test
    public void getMonthsWithoutAttendanceInfoByYear_currentYear() {
        getYearMonthIExpectationSetters(YEAR, NOVEMBER);
        getMonthValuesIExpectationSetters(hashSet(1, 3, 5, 7, 8, 9));
        replay();

        List<YearMonth> monthsWithoutAttendanceInfo = attendanceService.getMonthsWithoutAttendanceInfoByYear(YEAR);

        assertEquals(asList(
            YM_ENTRY_FEBRUARY,
            YM_ENTRY_APRIL,
            YM_ENTRY_JUNE,
            YM_ENTRY_OCTOBER
        ), monthsWithoutAttendanceInfo);
    }

    @Test
    public void getMonthsWithoutAttendanceInfoByYear_currentYear_allRequiredMonthWithAttendanceInfo() {
        getYearMonthIExpectationSetters(YEAR, NOVEMBER);
        getMonthValuesIExpectationSetters(hashSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        replay();

        List<YearMonth> monthsWithoutAttendanceInfo = attendanceService.getMonthsWithoutAttendanceInfoByYear(YEAR);

        assertEquals(emptyList(), monthsWithoutAttendanceInfo);
    }

    @Test
    public void getMonthsWithoutAttendanceInfoByYear_futureYear() {
        getYearMonthIExpectationSetters(YEAR, NOVEMBER);
        replay();

        List<YearMonth> monthsWithoutAttendanceInfo = attendanceService.getMonthsWithoutAttendanceInfoByYear(YEAR_NEXT);

        assertEquals(emptyList(), monthsWithoutAttendanceInfo);
    }

    @Test
    public void getMonthsWithoutAttendanceInfoByYear_futureCurrentJanuary() {
        getYearMonthIExpectationSetters(YEAR, JANUARY);
        replay();

        List<YearMonth> monthsWithoutAttendanceInfo = attendanceService.getMonthsWithoutAttendanceInfoByYear(YEAR);

        assertEquals(emptyList(), monthsWithoutAttendanceInfo);
    }

    @Test
    public void getMonthsWithoutAttendanceInfoByYear_forPastYear() {
        getYearMonthIExpectationSetters(YEAR_NEXT, JANUARY);
        getMonthValuesIExpectationSetters(hashSet(1, 3, 5, 7, 8, 9, 11));
        replay();

        List<YearMonth> monthsWithoutAttendanceInfo = attendanceService.getMonthsWithoutAttendanceInfoByYear(YEAR);

        assertEquals(asList(
            YM_ENTRY_FEBRUARY,
            YM_ENTRY_APRIL,
            YM_ENTRY_JUNE,
            YM_ENTRY_OCTOBER,
            YM_ENTRY_DECEMBER
        ), monthsWithoutAttendanceInfo);
    }

    private IExpectationSetters<Set<Integer>> getMonthValuesIExpectationSetters(Set<Integer> monthValues) {
        return expect(attendanceRepository.findMonthsValuesWithAttendanceInfoByYear(YEAR)).andReturn(monthValues);
    }

    private IExpectationSetters<YearMonth> getYearMonthIExpectationSetters(int year, Month january) {
        return expect(dateTimeService.getCurrentMonth()).andReturn(of(year, january));
    }
}
