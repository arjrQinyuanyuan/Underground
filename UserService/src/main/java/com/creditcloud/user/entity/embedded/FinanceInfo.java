/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creditcloud.user.entity.embedded;

import com.creditcloud.common.entities.BaseEntity;
import com.creditcloud.model.validation.group.LoanRequestCheck;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 *
 * @author rooseek
 */
@Embeddable
public class FinanceInfo extends BaseEntity {
    //是否有房

    @NotNull(message = "是否有房不能为空",
             groups = LoanRequestCheck.class)
    private boolean house;

    @NotNull(message = "房产数目不能为空",
             groups = LoanRequestCheck.class)
    @Column(name = "HOUSE_NUMBER")
    @Min(0)
    private int houseNumber;

    //有无房贷
    @NotNull(message = "有无房贷不能为空",
             groups = LoanRequestCheck.class)
    @Column(name = "HOUSE_LOAN")
    private boolean houseLoan;

    //是否有车
    @NotNull(message = "是否有车不能为空",
             groups = LoanRequestCheck.class)
    private boolean car;

    @NotNull(message = "车辆数目不能为空",
             groups = LoanRequestCheck.class)
    @Column(name = "CAR_NUMBER")
    @Min(0)
    private int carNumber;

    //有无车贷
    @NotNull(message = "有无车贷不能为空",
             groups = LoanRequestCheck.class)
    @Column(name = "CAR_LOAN")
    private boolean carLoan;

    public FinanceInfo() {
    }

    /**
     *
     * @param house 是否有房
     * @param houseNumber 房产套数
     * @param houseLoan 有无房贷
     * @param car 是否有车
     * @param carNumber 车辆数目
     * @param carLoan 有无车贷
     */
    public FinanceInfo(boolean house,
                       int houseNumber,
                       boolean houseLoan,
                       boolean car,
                       int carNumber,
                       boolean carLoan) {
        this.house = house;
        this.houseNumber = houseNumber;
        this.houseLoan = houseLoan;
        this.car = car;
        this.carNumber = carNumber;
        this.carLoan = carLoan;
    }

    public void setHouse(boolean house) {
        this.house = house;
    }

    public void setHouseLoan(boolean houseLoan) {
        this.houseLoan = houseLoan;
    }

    public void setCar(boolean car) {
        this.car = car;
    }

    public void setCarLoan(boolean carLoan) {
        this.carLoan = carLoan;
    }

    public boolean hasHouse() {
        return house;
    }

    public boolean hasHouseLoan() {
        return houseLoan;
    }

    public boolean hasCar() {
        return car;
    }

    public boolean hasCarLoan() {
        return carLoan;
    }

    public void setHouseNumber(int houseNumber) {
        this.houseNumber = houseNumber;
    }

    public void setCarNumber(int carNumber) {
        this.carNumber = carNumber;
    }

    public int getHouseNumber() {
        return houseNumber;
    }

    public int getCarNumber() {
        return carNumber;
    }
}
