package com.scoks.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scoks.order.Enums;
import com.scoks.order.dto.OrderQuery;
import com.scoks.order.entity.*;
import com.scoks.order.exception.ResultException;
import com.scoks.order.exception.ResultStatus;
import com.scoks.order.mapper.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class OrderServiceImpl {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;


    @Autowired
    private OrderProductMapper orderProductMapper;

    @Autowired
    private OrderProductWorkMapper orderProductWorkMapper;
    @Autowired
    private OrderProductFinalizeMapper orderProductFinalizeMapper;

    @Autowired
    private OrderProductOutMapper orderProductOutMapper;

    @Autowired
    private OrderProductOutWorkMapper orderProductOutWorkMapper;

    @Autowired
    private PartnerMapper partnerMapper;

    @Autowired
    private OrderMaterialMapper orderMaterialMapper;
    @Autowired
    private OrderMaterialLogMapper orderMaterialLogMapper;

    public Page<Order> findOrderPageList(Page<Order> page, OrderQuery form) {
        Subject currentUser = SecurityUtils.getSubject();
        Staff user = (Staff) currentUser.getPrincipal();
        if (form.getFinalizeState() == null && user.getPosition() == Enums.Position.FINALIZE.position()) {
            form.setFinalizeState(0);
        }
        if (form.getProduceState() == null && user.getPosition() == Enums.Position.DIRECTOR.position()) {
            form.setProduceState(0);
        }
        if (form.getOut() == null && user.getPosition() == Enums.Position.OUT.position()) {
            form.setOut(true);
        }
        if (user.getPosition() == Enums.Position.SALESMAN.position()) {
            form.setSalesmanId(user.getId());
        }
        List<Order> orders = orderMapper.listOrder(page, form);
        page.setRecords(orders);
        return page;
    }

    public Order getOrderById(Long id) {
        Order order = orderMapper.getOrder(id);
        if (order == null) {
            throw new ResultException(ResultStatus.ORDER_STATE_ERR);
        }
        Subject currentUser = SecurityUtils.getSubject();
        Staff user = (Staff) currentUser.getPrincipal();
        //外发专业看到的是 以外发单位维度展示的产品信息
        if (user.getPosition() == Enums.Position.OUT.position()) {
            OrderProduct where = new OrderProduct();
            where.setOrderId(id);
            where.setState(0);
            List<OrderProduct> orderProducts = orderProductMapper.listProductGroupOut(where);
            order.setProducts(orderProducts);
        } else {
            OrderProduct where = new OrderProduct();
            where.setOrderId(id);
            where.setState(0);
            List<OrderProduct> orderProducts = orderProductMapper.listProduct(where);
            order.setProducts(orderProducts);
        }

        return order;
    }

    @Transactional
    public long setOrder(Order form, long loginUid) throws NoSuchAlgorithmException {
        Long oid = form.getId();
        if (oid == null) {//新增
            long l = System.currentTimeMillis();
            form.setCreateTime(l);
            form.setUpdateTime(l);
            form.setCreator(loginUid);
            form.setState(0);
            if (form.getUrgentTime() == null) {
                form.setUrgentTime(0L);
            }

            int i = orderMapper.insertOrder(form);
            if (i == 0) {
                throw new ResultException(ResultStatus.ORDER_REPEAT);
            }
            OrderStatus orderStatus = new OrderStatus();
            orderStatus.setOrderId(form.getId());
            orderStatus.setCreateTime(System.currentTimeMillis());
            orderStatus.setUpdateTime(System.currentTimeMillis());
            orderStatusMapper.insert(orderStatus);
            oid = form.getId();
        } else {
            Order order = orderMapper.selectById(form.getId());
            if (order == null) {
                throw new ResultException(ResultStatus.ORDER_STATE_ERR);
            }
            if (form.getUrgent() != null && form.getUrgent() == 1) {
                form.setUrgentTime(System.currentTimeMillis());
            }
            form.setUpdateTime(System.currentTimeMillis());
            orderMapper.updateById(form);
        }
        return oid;

    }

    private int updateOrderTargetNum(long id, long num) {
        return orderStatusMapper.updateTargetNum(id, num, System.currentTimeMillis());
    }

    private int updateOrderFinalizeNum(long id, long num) {
        return orderStatusMapper.updateFinalizeNum(id, num, System.currentTimeMillis());
    }

    private int updateOrderOutTargetNum(long id, long num) {
        return orderStatusMapper.updateOutTargetNum(id, num, System.currentTimeMillis());
    }

    private int updateOrderCompletedNum(long id, long num) {
        return orderStatusMapper.updateCompletedNum(id, num, System.currentTimeMillis());

    }

    private int updateOrderOutCompletedNum(long id, long num) {
        return orderStatusMapper.updateOutCompletedNum(id, num, System.currentTimeMillis());

    }

    @Transactional
    public void setOrderPruduct(OrderProduct form) throws NoSuchAlgorithmException {
        if (form.getId() == null) {//新增
            Order order = orderMapper.selectById(form.getOrderId());
            if (order == null || order.getState() != 0) {
                throw new ResultException(ResultStatus.ORDER_STATE_ERR);
            }
            long l = System.currentTimeMillis();
            form.setCreateTime(l);
            form.setUpdateTime(l);
            form.setState(0);
            form.setCompletedNum(0L);
            orderProductMapper.insert(form);
            updateOrderTargetNum(form.getOrderId(), form.getTargetNum());
        } else {
            OrderProduct orderProduct = getOrderProduct(form.getId());
            if (orderProduct == null || orderProduct.getState() == 1) {
                throw new ResultException(ResultStatus.PRODUCT_STATE_ERR);
            }
            if (form.getTargetNum() != null && form.getTargetNum() < orderProduct.getOutTargetNum()) {
                throw new ResultException(ResultStatus.PRODUCT_OUT_LIMIT);
            }
            form.setOrderId(null);
            form.setCompletedNum(null);
            form.setUpdateTime(System.currentTimeMillis());
            orderProductMapper.updateById(form);
            if (orderProduct.getState() == 0 && form.getState() != null && form.getState() == 1) {
                updateOrderTargetNum(orderProduct.getOrderId(), -1 * orderProduct.getCompletedNum());
            } else if (form.getTargetNum() != null && orderProduct.getTargetNum() != form.getTargetNum().longValue()) {
                updateOrderTargetNum(orderProduct.getOrderId(), form.getTargetNum() - orderProduct.getTargetNum());
            }
        }

    }


    public Page<OrderProductWork> findOrderProductWorkPageList(Page<OrderProductWork> page, OrderProductWork where) {
        List<OrderProductWork> orderProductWorks = orderProductWorkMapper.listOrderProductWork(page, where);
        page.setRecords(orderProductWorks);
        return page;
    }

    private int updateProductCompletedNum(long productId, long num) {
        return orderProductMapper.updateCompletedNum(productId, num, System.currentTimeMillis());
    }

    private int updateProductFinalizeNum(long productId, long num) {
        return orderProductMapper.updateFinalizeNum(productId, num, System.currentTimeMillis());
    }

    @Transactional
    public void setOrderProductWork(OrderProductWork form) {
        if (form.getId() == null) {//新增
            if (form.getCompleted() == null || form.getCompleted() <= 0) {
                throw new ResultException(ResultStatus.PARAM_ERR);
            }
            OrderProduct orderProduct = orderProductMapper.selectById(form.getProductId());
            if (orderProduct == null || orderProduct.getState() != 0) {
                throw new ResultException(ResultStatus.PRODUCT_STATE_ERR);
            }

            Order order = orderMapper.selectById(orderProduct.getOrderId());
            if (order == null || order.getState() != 0) {
                throw new ResultException(ResultStatus.ORDER_STATE_ERR);
            }

            form.setOrderId(orderProduct.getOrderId());
            long l = System.currentTimeMillis();
            form.setCreateTime(l);
            form.setUpdateTime(l);
            form.setState(0);
            int insert = orderProductWorkMapper.insert(form);
            if (insert > 0) {
                updateProductCompletedNum(orderProduct.getId(), form.getCompleted());
                updateOrderCompletedNum(orderProduct.getOrderId(), form.getCompleted());
            }

        } else {
            if (form.getCompleted() != null && form.getCompleted() <= 0) {
                throw new ResultException(ResultStatus.PARAM_ERR);
            }
            OrderProductWork orderProductWork = orderProductWorkMapper.selectById(form.getId());
            Order order = orderMapper.selectById(orderProductWork.getOrderId());
            if (order == null || order.getState() != 0) {
                throw new ResultException(ResultStatus.ORDER_STATE_ERR);
            }
            OrderProduct orderProduct = orderProductMapper.selectById(orderProductWork.getProductId());
            if (orderProduct == null || orderProduct.getState() != 0) {
                throw new ResultException(ResultStatus.PRODUCT_STATE_ERR);
            }
            form.setUpdateTime(System.currentTimeMillis());
            int i = orderProductWorkMapper.updateById(form);
            if (i == 0) {
                throw new ResultException(ResultStatus.UPDATE_ERR);
            }
            if (orderProductWork.getState() == 0 && form.getState() != null && form.getState() == 1) {
                updateProductCompletedNum(orderProduct.getId(), -1 * orderProductWork.getCompleted());
                updateOrderCompletedNum(orderProduct.getOrderId(), -1 * orderProductWork.getCompleted());

            } else if (form.getCompleted() != null && orderProductWork.getCompleted() != form.getCompleted().longValue()) {
                updateProductCompletedNum(orderProduct.getId(), form.getCompleted() - orderProductWork.getCompleted());
                updateOrderCompletedNum(orderProduct.getOrderId(), form.getCompleted() - orderProductWork.getCompleted());

            }
        }

    }


    public OrderProduct getOrderProduct(Long id) {
        return orderProductMapper.selectById(id);
    }

    public OrderProductFinalize getOrderFinalize(Long id) {
        return orderProductFinalizeMapper.selectById(id);
    }

    public Page<OrderProductFinalize> findOrderProductFinalizePageList(Page<OrderProductFinalize> page, OrderProductFinalize where) {
        List<OrderProductFinalize> orderProductWorks = orderProductFinalizeMapper.listOrderProductFinalize(page, where);
        page.setRecords(orderProductWorks);
        return page;
    }

    @Transactional
    public void setOrderProductFinalize(OrderProductFinalize form) {
        if (form.getId() == null) {//新增
            if (form.getCompleted() == null || form.getCompleted() <= 0) {
                throw new ResultException(ResultStatus.PARAM_ERR);
            }

            OrderProduct orderProduct = orderProductMapper.selectById(form.getProductId());
            if (orderProduct == null || orderProduct.getState() != 0) {
                throw new ResultException(ResultStatus.PRODUCT_STATE_ERR);

            }
            Order order = orderMapper.selectById(orderProduct.getOrderId());
            if (order == null || order.getState() != 0) {
                throw new ResultException(ResultStatus.ORDER_STATE_ERR);
            }

            form.setOrderId(orderProduct.getOrderId());
            long l = System.currentTimeMillis();
            form.setCreateTime(l);
            form.setUpdateTime(l);
            form.setState(0);
            int insert = orderProductFinalizeMapper.insert(form);
            if (insert > 0) {
                updateProductFinalizeNum(orderProduct.getId(), form.getCompleted());
                updateOrderFinalizeNum(orderProduct.getOrderId(), form.getCompleted());
            }
        } else {
            if (form.getCompleted() != null && form.getCompleted() <= 0) {
                throw new ResultException(ResultStatus.PARAM_ERR);
            }
            OrderProductFinalize orderProductFinalize = orderProductFinalizeMapper.selectById(form.getId());
            Order order = orderMapper.selectById(orderProductFinalize.getOrderId());
            if (order == null || order.getState() != 0) {
                throw new ResultException(ResultStatus.ORDER_STATE_ERR);
            }
            OrderProduct orderProduct = orderProductMapper.selectById(orderProductFinalize.getProductId());
            if (orderProduct == null || orderProduct.getState() != 0) {
                throw new ResultException(ResultStatus.PRODUCT_STATE_ERR);
            }
            form.setUpdateTime(System.currentTimeMillis());
            int i = orderProductFinalizeMapper.updateById(form);
            if (i == 0) {
                throw new ResultException(ResultStatus.UPDATE_ERR);
            }

            if (orderProductFinalize.getState() == 0 && form.getState() != null && form.getState() == 1) {
                updateProductFinalizeNum(orderProduct.getId(), -1 * orderProductFinalize.getCompleted());
                updateOrderFinalizeNum(orderProduct.getOrderId(), -1 * orderProductFinalize.getCompleted());
            } else if (form.getCompleted() != null && orderProductFinalize.getCompleted() != form.getCompleted().longValue()) {
                updateProductFinalizeNum(orderProduct.getId(), form.getCompleted() - orderProductFinalize.getCompleted());
                updateOrderFinalizeNum(orderProduct.getOrderId(), form.getCompleted() - orderProductFinalize.getCompleted());
            }
        }

    }


    public OrderProductOut getOrderProductOut(Long id) {
        OrderProductOut orderProductOut = orderProductOutMapper.selectById(id);
        Partner partner = partnerMapper.selectById(orderProductOut.getPartnerId());
        if (partner != null) {
            orderProductOut.setPartnerName(partner.getName());
        }
        return orderProductOut;
    }

    public Page<OrderProductOut> findOrderProductOutPageList(Page<OrderProductOut> page, OrderProductOut where) {
        List<OrderProductOut> orderProductOuts = orderProductOutMapper.pageProductOut(page, where);
        page.setRecords(orderProductOuts);
        return page;
    }

    @Transactional
    public void setOrderProductOut(OrderProductOut form) {
        if (form.getId() == null) {//新增
            if (form.getTargetNum() == null || form.getTargetNum() <= 0) {
                throw new ResultException(ResultStatus.PARAM_ERR);
            }
            OrderProduct orderProduct = orderProductMapper.selectById(form.getProductId());
            if (orderProduct == null || orderProduct.getState() != 0) {
                throw new ResultException(ResultStatus.PRODUCT_STATE_ERR);

            }
            Order order = orderMapper.selectById(orderProduct.getOrderId());
            if (order == null || order.getState() != 0) {
                throw new ResultException(ResultStatus.ORDER_STATE_ERR);
            }

            form.setOrderId(orderProduct.getOrderId());
            long l = System.currentTimeMillis();
            form.setCreateTime(l);
            form.setUpdateTime(l);
            form.setState(0);
            form.setCompletedNum(0L);
            int insert = orderProductOutMapper.insert(form);
            if (insert > 0) {
                int i = orderProductMapper.updateOutTargetNum(orderProduct.getId(), form.getTargetNum(), System.currentTimeMillis());
                if (i == 0) {
                    throw new ResultException(ResultStatus.PRODUCT_OUT_LIMIT);
                }
                updateOrderOutTargetNum(orderProduct.getOrderId(), form.getTargetNum());
            }
        } else {
            if (form.getTargetNum() != null && form.getTargetNum() <= 0) {
                throw new ResultException(ResultStatus.PARAM_ERR);
            }
            OrderProductOut orderProductOut = orderProductOutMapper.selectById(form.getId());
            Order order = orderMapper.selectById(orderProductOut.getOrderId());
            if (order == null || order.getState() != 0) {
                throw new ResultException(ResultStatus.ORDER_STATE_ERR);
            }
            OrderProduct orderProduct = orderProductMapper.selectById(orderProductOut.getProductId());
            if (orderProduct == null || orderProduct.getState() != 0) {
                throw new ResultException(ResultStatus.PRODUCT_STATE_ERR);
            }
            form.setCompletedNum(null);
            form.setUpdateTime(System.currentTimeMillis());
            int i = orderProductOutMapper.updateById(form);
            if (i == 0) {
                throw new ResultException(ResultStatus.UPDATE_ERR);
            }

            //删除外发记录
            if (orderProductOut.getState() == 0 && form.getState() != null && form.getState() == 1) {
                orderProductMapper.updateOutTargetNum(orderProduct.getId(), -1 * orderProductOut.getTargetNum(), System.currentTimeMillis());
                orderProductMapper.updateOutCompletedNum(orderProduct.getId(), -1 * orderProductOut.getCompletedNum(), System.currentTimeMillis());
                updateOrderOutCompletedNum(orderProduct.getOrderId(), -1 * orderProductOut.getCompletedNum());
                updateOrderOutTargetNum(orderProduct.getOrderId(), -1 * orderProductOut.getTargetNum());
            } else if (form.getTargetNum() != null && orderProductOut.getTargetNum() != form.getTargetNum()) {
                i = orderProductMapper.updateOutTargetNum(orderProduct.getId(), form.getTargetNum() - orderProductOut.getTargetNum(), System.currentTimeMillis());
                if (i == 0) {
                    throw new ResultException(ResultStatus.PRODUCT_OUT_LIMIT);
                }
                updateOrderOutTargetNum(orderProduct.getOrderId(), form.getTargetNum() - orderProductOut.getTargetNum());
            }
        }
    }


    private int updateProductOutCompletedNum(long id, long num) {
        OrderProductOut where = new OrderProductOut();
        where.setId(id);
        UpdateWrapper<OrderProductOut> updateWrapper = new UpdateWrapper<>(where);
        updateWrapper.set("update_time", System.currentTimeMillis());
        updateWrapper.setSql("completed_num = completed_num + " + num);
        return orderProductOutMapper.update(null, updateWrapper);
    }


    public OrderProductOutWork getOrderProductOutWork(Long id) {
        return orderProductOutWorkMapper.selectById(id);
    }

    public Page<OrderProductOutWork> findOrderProductOutWorkPageList(Page<OrderProductOutWork> page, OrderProductOutWork where) {
        orderProductOutWorkMapper.selectPage(page, new QueryWrapper<>(where));
        return page;
    }


    @Transactional
    public void setOrderProductOutWork(OrderProductOutWork form) {
        if (form.getId() == null) {//新增
            if (form.getCompleted() == null || form.getCompleted() <= 0) {
                throw new ResultException(ResultStatus.PARAM_ERR);
            }
            OrderProductOut orderProductOut = orderProductOutMapper.selectById(form.getProductOutId());
            if (orderProductOut == null || orderProductOut.getState() != 0) {
                throw new ResultException(ResultStatus.PRODUCT_STATE_ERR);
            }

            Order order = orderMapper.selectById(orderProductOut.getOrderId());
            if (order == null || order.getState() != 0) {
                throw new ResultException(ResultStatus.ORDER_STATE_ERR);
            }
            OrderProduct orderProduct = orderProductMapper.selectById(orderProductOut.getProductId());
            if (orderProduct == null || orderProduct.getState() != 0) {
                throw new ResultException(ResultStatus.PRODUCT_STATE_ERR);
            }
            form.setOrderId(orderProductOut.getOrderId());
            form.setProductId(orderProductOut.getProductId());
            long l = System.currentTimeMillis();
            form.setCreateTime(l);
            form.setUpdateTime(l);
            form.setState(0);
            int insert = orderProductOutWorkMapper.insert(form);
            if (insert > 0) {
                updateProductOutCompletedNum(orderProductOut.getId(), form.getCompleted());
                orderProductMapper.updateOutCompletedNum(orderProductOut.getProductId(), form.getCompleted(), System.currentTimeMillis());
                updateOrderOutCompletedNum(orderProductOut.getOrderId(), form.getCompleted());
            }
        } else {
            if (form.getCompleted() != null && form.getCompleted() <= 0) {
                throw new ResultException(ResultStatus.PARAM_ERR);
            }
            OrderProductOutWork orderProductOutWork = orderProductOutWorkMapper.selectById(form.getId());
            Order order = orderMapper.selectById(orderProductOutWork.getOrderId());
            if (order == null || order.getState() != 0) {
                throw new ResultException(ResultStatus.ORDER_STATE_ERR);
            }
            OrderProduct orderProduct = orderProductMapper.selectById(orderProductOutWork.getProductId());
            if (orderProduct == null || orderProduct.getState() != 0) {
                throw new ResultException(ResultStatus.PRODUCT_STATE_ERR);
            }
            OrderProductOut orderProductOut = orderProductOutMapper.selectById(orderProductOutWork.getProductOutId());
            if (orderProductOut == null || orderProductOut.getState() != 0) {
                throw new ResultException(ResultStatus.PRODUCT_STATE_ERR);
            }
            form.setUpdateTime(System.currentTimeMillis());
            int i = orderProductOutWorkMapper.updateById(form);
            if (i == 0) {
                throw new ResultException(ResultStatus.UPDATE_ERR);
            }

            //删除外发完成记录
            if (orderProductOutWork.getState() == 0 && form.getState() != null && form.getState() == 1) {
                updateProductOutCompletedNum(orderProductOut.getId(), -1 * orderProductOutWork.getCompleted());
                orderProductMapper.updateOutCompletedNum(orderProductOut.getProductId(), -1 * orderProductOutWork.getCompleted(), System.currentTimeMillis());
                updateOrderOutCompletedNum(orderProductOut.getOrderId(), -1 * orderProductOutWork.getCompleted());

            } else if (form.getCompleted() != null && orderProductOutWork.getCompleted() != form.getCompleted()) {
                long updateNum = form.getCompleted() - orderProductOutWork.getCompleted();
                updateProductOutCompletedNum(orderProductOut.getId(), updateNum);
                orderProductMapper.updateOutCompletedNum(orderProductOut.getProductId(), updateNum, System.currentTimeMillis());
                updateOrderOutCompletedNum(orderProductOut.getOrderId(), updateNum);

            }
        }
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        orderStatus.setUpdateTime(System.currentTimeMillis());
        orderStatusMapper.updateById(orderStatus);
    }

    public Page<OrderMaterial> findOrderMaterialPageList(Page<OrderMaterial> page, OrderMaterial form) {
        List<OrderMaterial> materials = orderMaterialMapper.listOrderMaterial(page, form);
        page.setRecords(materials);
        return page;
    }


    public void insertOrUpdateMaterial(OrderMaterial form) {
        if (form.getId() == null) {//新增
            long l = System.currentTimeMillis();
            form.setCreateTime(l);
            form.setUpdateTime(l);
            form.setGetNum(0L);
            orderMaterialMapper.insert(form);
        } else {
            form.setOrderId(null);
            form.setGetNum(null);
            form.setUpdateTime(System.currentTimeMillis());
            orderMaterialMapper.updateById(form);
        }
    }

    public Page<OrderMaterialLog> findOrderMaterialLogPageList(Page<OrderMaterialLog> page, OrderMaterialLog form) {
        List<OrderMaterialLog> materials = orderMaterialLogMapper.listOrderMaterialLogs(page, form);
        page.setRecords(materials);
        return page;
    }

    @Transactional
    public void insertOrUpdateMaterialLog(OrderMaterialLog form) {
        if (form.getId() == null) {//新增
            long l = System.currentTimeMillis();
            form.setCreateTime(l);
            form.setUpdateTime(l);
            orderMaterialLogMapper.insert(form);
            orderMaterialMapper.updateGetNum(form.getOrderMaterialId(), form.getGetNum());
        } else {
            if (form.getGetNum() == null) {
                return;
            }
            OrderMaterialLog orderMaterialLog = orderMaterialLogMapper.selectById(form.getId());
            form.setOrderMaterialId(null);
            form.setUpdateTime(System.currentTimeMillis());
            orderMaterialLogMapper.updateById(form);
            if (orderMaterialLog.getGetNum() != form.getGetNum()) {
                orderMaterialMapper.updateGetNum(orderMaterialLog.getOrderMaterialId(), form.getGetNum() - orderMaterialLog.getGetNum());
            }

        }
    }
}
