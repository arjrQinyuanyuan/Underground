/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creditcloud.carinsurance;

import com.creditcloud.carinsurance.api.CarInsuranceRequestService;
import com.creditcloud.carinsurance.api.CarInsuranceService;
import com.creditcloud.carinsurance.entities.CarInsuranceRequest;
import com.creditcloud.carinsurance.entities.dao.CarInsuranceDAO;
import com.creditcloud.carinsurance.entities.dao.CarInsuranceRequestDAO;
import com.creditcloud.carinsurance.local.ApplicationBean;
import com.creditcloud.carinsurance.model.CarInsuranceModel;
import com.creditcloud.carinsurance.model.CarInsuranceRequestModel;
import com.creditcloud.carinsurance.model.enums.CarInsuranceChagreBackType;
import com.creditcloud.carinsurance.model.enums.CarInsurancePayStatus;
import com.creditcloud.carinsurance.model.enums.CarInsuranceRequestStatus;
import com.creditcloud.carinsurance.model.enums.CarInsuranceStatus;
import com.creditcloud.carinsurance.utils.CarInsuranceDTOUtils;
import com.creditcloud.client.api.EmployeeService;
import com.creditcloud.config.api.ConfigManager;
import com.creditcloud.model.client.Employee;
import com.creditcloud.model.constant.EmailConstant;
import com.creditcloud.model.criteria.PageInfo;
import com.creditcloud.model.enums.Source;
import com.creditcloud.model.user.User;
import com.creditcloud.user.api.UserService;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.slf4j.Logger;

/**
 * 车险申请
 *
 * @author Administrator
 */
@Remote
@Stateless
public class CarInsuranceRequestServiceBean implements CarInsuranceRequestService {

    @EJB
    ApplicationBean appBean;

    @Inject
    Logger logger;

    @EJB
    UserService userService;

    @EJB
    private CarInsuranceDAO carInsuranceDAO;

    @EJB
    private CarInsuranceRequestDAO carInsuranceRequestDAO;

    @EJB
    private CarInsuranceService carInsuranceService;

    @EJB
    private EmployeeService employeeService;

    @EJB
    private ConfigManager configManager;

    /**
     * 保存
     *
     * @param model
     * @return
     */
    @Override
    public CarInsuranceRequestModel create(CarInsuranceRequestModel model) {
	CarInsuranceRequest request = CarInsuranceDTOUtils.convertCarInsuranceRequest(model);
	CarInsuranceRequest result = carInsuranceRequestDAO.create(request);

	logger.debug("CarInsuranceRequest persist success:\n{}", result);
	return CarInsuranceDTOUtils.convertCarInsuranceRequestDTO(result);
    }

    /**
     * 更新
     *
     * @param model
     */
    @Override
    public void edit(CarInsuranceRequestModel model) {
	CarInsuranceRequest request = CarInsuranceDTOUtils.convertCarInsuranceRequest(model);
	if (request != null) {
	    logger.debug("edit CarInsuranceRequest success :\n {}", request);
	    carInsuranceRequestDAO.edit(request);
	} else {
	    logger.error("edit CarInsuranceRequest failure :\n{}", request);
	}

    }

    /**
     * editByWebservice
     */
    @Override
    public void editByWebservice(CarInsuranceRequestModel model) {
	CarInsuranceRequest request = carInsuranceRequestDAO.findByNum(model.getInsuranceNum());
	if (request != null) {
	    logger.debug("editByWebservice CarInsuranceRequest success :\n {}", request);
	    //可以修改的字段
	    request.setTitle(model.getTitle());
	    request.setName(model.getName());
	    request.setIdNumber(model.getIdNumber());
	    request.setMobile(model.getMobile());
	    request.setTotalAmount(model.getTotalAmount());
	    request.setAmount(model.getAmount());
	    request.setFirstPayment(model.getFirstPayment());
	    request.setRate(model.getRate());
	    request.setDuration(model.getDuration());
	    request.setDurationType(model.getDurationType());
	    request.setCarInsuranceType(model.getCarInsuranceType());
	    carInsuranceRequestDAO.edit(request);
	} else {
	    logger.error("edit CarInsuranceRequest failure :\n{}", request);
	}

    }

    /**
     * Cancel 取消
     *
     * @param model
     */
    @Override
    public boolean cancel(String insuranceNum) {
	boolean bool = true;
	CarInsuranceRequest request = carInsuranceRequestDAO.findByNum(insuranceNum);
	if (request != null) {
	    logger.debug("editByWebservice CarInsuranceRequest success :\n {}", request);
	    //可以修改的字段
	    request.setCarInsuranceRequestStatus(CarInsuranceRequestStatus.CANCELED);
	    carInsuranceRequestDAO.edit(request);
	} else {
	    logger.error("edit CarInsuranceRequest failure insuranceNum :{}", insuranceNum);
	    bool = false;
	}
	return bool;
    }

    /**
     * 确认
     *
     * @param insuranceNum
     * @return
     */
    @Override
    public boolean confirm(String insuranceNum) {
	boolean bool = true;
	CarInsuranceRequest request = carInsuranceRequestDAO.findByNum(insuranceNum);
	logger.debug("CarInsuranceRequest  request confirm#####{}", request);
	//没有确认的状态和支付成功才可以投保
	if (request != null
		&& request.getCarInsuranceRequestStatus() != CarInsuranceRequestStatus.COMFIRM
		&& request.getCarInsurancePayStatus() == CarInsurancePayStatus.PAYSUCCESS) {
	    logger.debug("editByWebservice CarInsuranceRequest success :\n {}", request);
	    //首先得获取到用户employee admin的Id
	    String empId = configManager.getCarInsuranceConfig().getEmpployeeId();
	    Employee employee = employeeService.findById(appBean.getClientCode(), empId);
	    if (employee != null) {
		//接受车险分期确认,创建用户和生成还款计划
		bool = approve(employee, request.getId());
		//如果创建用户成功修改状态为已确认
		request.setCarInsuranceRequestStatus(CarInsuranceRequestStatus.COMFIRM);
		carInsuranceRequestDAO.edit(request);
	    } else {
		logger.error("when car insurance approve,employee not exist  empID:{}", empId);
		bool = false;
	    }
	} else {
	    logger.error("edit CarInsuranceRequest failure insuranceNum :{}", insuranceNum);
	    bool = false;
	}
	return bool;
    }

    /**
     * 根据车险申请的状态信息 获取列表
     *
     * @param status CarInsuranceRequestStatus
     * @return
     */
    @Override
    public List<CarInsuranceRequestModel> listAllByStatus(CarInsuranceRequestStatus... status) {
	List<CarInsuranceRequestModel> result = new ArrayList<>();
	List<CarInsuranceRequest> list = carInsuranceRequestDAO.listByStatus(PageInfo.ALL, status).getResults();
	for (CarInsuranceRequest request : list) {
	    result.add(CarInsuranceDTOUtils.convertCarInsuranceRequestDTO(request));
	}
	return result;
    }

    /**
     * 根据id 获取一个对象
     *
     * @param id
     * @return
     */
    @Override
    public CarInsuranceRequestModel getCarInsuranceRequestModelById(String id) {
	CarInsuranceRequest request = carInsuranceRequestDAO.find(id);
	CarInsuranceRequestModel model = CarInsuranceDTOUtils.convertCarInsuranceRequestDTO(request);
	return model;
    }

    @Override
    public boolean approve(Employee employee, String requestId) {
	logger.debug("approve CarInsuranceRequest called.[employee={}]", employee, requestId);
	//update loan request status
	CarInsuranceRequest request = carInsuranceRequestDAO.find(requestId);
	if (request != null) {
//	    String loginName = MobileConstant.MOBILE_USER_LOGINNAME_PREFIX.concat(request.getMobile());
//	    修改2014-1-15 如果该用户手机号已经投过保 则使用原来的账户
	    User user = userService.findByMobile(appBean.getClientCode(), request.getMobile());
	    if (user == null) {
		String loginName = "arjr".concat(request.getMobile());
		logger.debug("车险分期创建的 loginname:{}", loginName);
		//1 创建一个新用户，
		User newUser = new User(null,
			appBean.getClientCode(),
			request.getName(),
			loginName,
			request.getIdNumber(),
			request.getMobile(),
			EmailConstant.DEFAULT_EMAIL,
			Source.BACK,
			employee.getId(),
			employee.getId());
		try {
		    user = userService.addUser(appBean.getClientCode(), newUser);
		} catch (Exception ex) {
		    logger.error("在车险分期中,自动创建该用户失败.\n{}", user);
		    return false;
		}//add user
	    }
	    //2 添加一个车险分期信息
	    CarInsuranceModel model = new CarInsuranceModel(
		    "",
		    request.getInsuranceNum(),
		    user,
		    user.getLoginName(),
		    user.getName(),
		    user.getMobile(),
		    request.getAmount(),
		    request.getRate(),
		    request.getCarInsuranceType(),
		    request.getFirstPayment(),
		    request.getDuration(),
		    request.getDurationType(),
		    CarInsuranceChagreBackType.SUCESS,
		    request.getAcceptanceDate(),
		    CarInsuranceStatus.PAYING,
		    request.getTitle(),
		    request.getTotalAmount());
	    //创建一个车险分期，在创建车险分期的同时生成还款计划
	    carInsuranceService.create(model);
	    logger.debug("车险分期批准通过.\n{}", model);
	    //修改申请标的状态为已批准
	    request.setCarInsuranceRequestStatus(CarInsuranceRequestStatus.APPROVED);
	    carInsuranceRequestDAO.edit(request);
	    
	    //3 该车险申请成功后,发送短信给用户
	    /*
	     * 在短信恢复正常前暂不发送该类信息 smsService.sendMessage(appBean.getClient(),
	     * SMSType.NOTIFICATION_LOANREQUEST_STATUS,
	     * userBridge.getMobile(request.getUserId()),
	     * TimeConstant.SIMPLE_CHINESE_DATE_FORMAT.format(request.getTimeSubmit()),
	     * request.getTitle(), LoanRequestStatus.APPROVED.getKey());
	     */
	    return true;
	} else {
	    logger.error("CarInsuranceRequest not exist or deleted. CarInsuranceRequest Id :{}", requestId);
	    return false;
	}

    }

    /**
     * 根据保单号查询
     *
     * @param insuranceNum
     * @return
     */
    @Override
    public CarInsuranceRequestModel findByNum(String insuranceNum) {
	CarInsuranceRequest request = carInsuranceRequestDAO.findByNum(insuranceNum);
	CarInsuranceRequestModel model = CarInsuranceDTOUtils.convertCarInsuranceRequestDTO(request);
	return model;
    }

    /**
     * 支付通知接口
     *
     * @param insuranceNum
     * @return
     */
    @Override
    public boolean changePayStatus(String insuranceNum, CarInsurancePayStatus payStatus) {
	boolean bool = true;
	CarInsuranceRequest request = carInsuranceRequestDAO.findByNum(insuranceNum);
	if (request != null) {
	    logger.debug("payNotice CarInsuranceRequest success :\n {}", request);
	    //可以修改的字段
	    request.setCarInsurancePayStatus(payStatus);
	    carInsuranceRequestDAO.edit(request);
	} else {
	    logger.error("payNotice CarInsuranceRequest failure insuranceNum :{}", insuranceNum);
	    bool = false;
	}
	return bool;
    }

}
