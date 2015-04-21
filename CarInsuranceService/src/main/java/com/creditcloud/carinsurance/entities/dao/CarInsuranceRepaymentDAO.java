package com.creditcloud.carinsurance.entities.dao;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.creditcloud.carinsurance.entities.CarInsurance;
import com.creditcloud.carinsurance.entities.CarInsuranceRepayment;
import com.creditcloud.carinsurance.local.ApplicationBean;
import com.creditcloud.carinsurance.model.enums.CarInsuranceStatus;
import com.creditcloud.common.entities.dao.AbstractDAO;
import com.creditcloud.model.criteria.PageInfo;
import com.creditcloud.model.misc.PagedResult;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.joda.time.LocalDate;
import org.slf4j.Logger;

/**
 *
 * @author wangwei
 */
@Stateless
@LocalBean
public class CarInsuranceRepaymentDAO extends AbstractDAO<CarInsuranceRepayment> {

    @Inject
    Logger logger;

    @EJB
    ApplicationBean appBean;

    @PersistenceContext(unitName = "ManagerPU")
    private EntityManager em;

    public CarInsuranceRepaymentDAO() {
	super(CarInsuranceRepayment.class);
    }

    @Override
    protected EntityManager getEntityManager() {
	return em;
    }

    /**
     * 根据保单号查询还款计划
     *
     * @param insuranceNum
     * @return
     */
    public List<CarInsuranceRepayment> listInsuranceByNum(String insuranceNum) {
	List<CarInsuranceRepayment> list = getEntityManager().createNamedQuery("CarInsuranceRepayment.listCarInsuranceByNum", CarInsuranceRepayment.class)
		.setParameter("insuranceNum", insuranceNum).getResultList();
	if (null != list && list.size() > 0) {
	    return list;
	} else {
	    return null;
	}
    }

    /**
     * 根据ID查询该条还款计划
     *
     * @param id
     * @return
     */
    public CarInsuranceRepayment findById(String id) {
	List<CarInsuranceRepayment> list = getEntityManager().createNamedQuery("CarInsuranceRepayment.findById", CarInsuranceRepayment.class)
		.setParameter("id", id).getResultList();
	if (null != list && list.size() > 0) {
	    return list.get(0);
	} else {
	    return null;
	}
    }

    /**
     * 列出一段时间类到期的 总记录
     *
     * @param from
     * @param to
     * @param status
     * @return
     */
    int countCarInsuranceDueRepayByUser(String userId, Date from, Date to, CarInsuranceStatus... status) {
	if (status == null || status.length == 0) {
	    return 0;
	}
	Long result = getEntityManager()
		.createNamedQuery("CarInsuranceRepayment.countCarInsuranceDueRepayByUser", Long.class)
		.setParameter("userId", userId)
		.setParameter("from", from)
		.setParameter("to", to)
		.setParameter("statusList", Arrays.asList(status))
		.getSingleResult();
	return result == null ? 0 : result.intValue();
    }

    /**
     * 列出一段时间类到期的 车险还款计划 根据用户查
     *
     * @param from
     * @param to
     * @param info
     * @param status
     * @return
     */
    public PagedResult<CarInsuranceRepayment> listCarInsuranceDueRepayByUser(String userId, Date from, Date to, PageInfo info, CarInsuranceStatus... status) {
	if (status == null || status.length == 0) {
	    return new PagedResult<>(Collections.EMPTY_LIST, 0);
	}
	Query query = getEntityManager()
		.createNamedQuery("CarInsuranceRepayment.listCarInsuranceDueRepayByUser", CarInsuranceRepayment.class)
		.setParameter("userId", userId)
		.setParameter("from", from)
		.setParameter("to", to)
		.setParameter("statusList", Arrays.asList(status));

	query.setFirstResult(info.getOffset());
	query.setMaxResults(info.getSize());

	int totalSize = countCarInsuranceDueRepay(userId,from, to, status);
	return new PagedResult(query.getResultList(), totalSize);
    }

    /**
     * 列出一段时间类到期的 总记录
     *
     * @param from
     * @param to
     * @param status
     * @return
     */
    int countCarInsuranceDueRepay(String userId,Date from, Date to, CarInsuranceStatus... status) {
	if (status == null || status.length == 0) {
	    return 0;
	}
	Long result = getEntityManager()
		.createNamedQuery("CarInsuranceRepayment.countCarInsuranceDueRepayByUser", Long.class)
		.setParameter("userId", userId)
		.setParameter("from", from)
		.setParameter("to", to)
		.setParameter("statusList", Arrays.asList(status))
		.getSingleResult();
	return result == null ? 0 : result.intValue();
    }

    /**
     * 列出一段时间类到期的 车险还款计划 查询所有
     *
     * @param from
     * @param to
     * @param info
     * @param status
     * @return
     */
    public PagedResult<CarInsuranceRepayment> listCarInsuranceDueRepay(Date from, Date to, PageInfo info, CarInsuranceStatus... status) {
	if (status == null || status.length == 0) {
	    return new PagedResult<>(Collections.EMPTY_LIST, 0);
	}
	Query query = getEntityManager()
		.createNamedQuery("CarInsuranceRepayment.listCarInsuranceDueRepay", CarInsuranceRepayment.class)
		.setParameter("from", from)
		.setParameter("to", to)
		.setParameter("statusList", Arrays.asList(status));

	query.setFirstResult(info.getOffset());
	query.setMaxResults(info.getSize());

	int totalSize = countCarInsuranceDueRepay(from, to, status);
	return new PagedResult(query.getResultList(), totalSize);
    }

     int countCarInsuranceDueRepay(Date from, Date to, CarInsuranceStatus... status) {
	if (status == null || status.length == 0) {
	    return 0;
	}
	Long result = getEntityManager()
		.createNamedQuery("CarInsuranceRepayment.countCarInsuranceDueRepay", Long.class)
		.setParameter("from", from)
		.setParameter("to", to)
		.setParameter("statusList", Arrays.asList(status))
		.getSingleResult();
	return result == null ? 0 : result.intValue();
    }
    /**
     * 根据车险分期 获取该还款计划
     *
     * @param carInsurance
     * @return
     */
    public List<CarInsuranceRepayment> listByCarInsurance(CarInsurance carInsurance) {
	Query query = getEntityManager()
		.createNamedQuery("CarInsuranceRepayment.listCarInsuranceByCarInsurance", CarInsuranceRepayment.class)
		.setParameter("carInsurance", carInsurance);
	return query.getResultList();
    }

    /**
     * 根据订单id获取
     *
     * @param orderId
     * @return
     */
    public CarInsuranceRepayment findByOrderId(String orderId) {
	CarInsuranceRepayment result = null;
	logger.debug("findByOrderId ：{}", orderId);
	result = getEntityManager()
		.createNamedQuery("CarInsuranceRepayment.findByOrderId", CarInsuranceRepayment.class)
		.setParameter("orderId", orderId)
		.getSingleResult();
	return result;
    }

    /**
     * 按时间区间和还款状态列出一段时间内所有到期的CarInsuranceRepayment
     *
     * @param from
     * @param to
     * @param status
     * @return
     */
    public PagedResult<CarInsuranceRepayment> listDueRepay(Date from, Date to, PageInfo info, CarInsuranceStatus... status) {
	if (status == null || status.length == 0) {
	    return new PagedResult<>(Collections.EMPTY_LIST, 0);
	}
	Query query = getEntityManager()
		.createNamedQuery("CarInsuranceRepayment.listDueRepay", CarInsuranceRepayment.class)
		.setParameter("from", from)
		.setParameter("to", to)
		.setParameter("statusList", Arrays.asList(status));

	query.setFirstResult(info.getOffset());
	query.setMaxResults(info.getSize());

	int totalSize = countDueRepay(from, to, status);
	return new PagedResult(query.getResultList(), totalSize);
    }

    /**
     * 按时间区间和还款状态统计一段时间内所有到期的CarInsuranceRepayment
     *
     * @param from
     * @param to
     * @param status
     * @return
     */
    int countDueRepay(Date from, Date to, CarInsuranceStatus... status) {
	if (status == null || status.length == 0) {
	    return 0;
	}
	Long result = getEntityManager()
		.createNamedQuery("CarInsuranceRepayment.countDueRepay", Long.class)
		.setParameter("from", from)
		.setParameter("to", to)
		.setParameter("statusList", Arrays.asList(status))
		.getSingleResult();
	return result == null ? 0 : result.intValue();
    }

    /**
     * update loan status
     *
     * @param status
     * @param ids
     * @return
     */
    public boolean markStatus(CarInsuranceStatus status, String... ids) {
	if (ids == null || ids.length == 0) {
	    return false;
	}
	int result = getEntityManager()
		.createNamedQuery("CarInsuranceRepayment.markStatus")
		.setParameter("status", status)
		.setParameter("ids", Arrays.asList(ids))
		.executeUpdate();
	return result > 0;
    }

}
