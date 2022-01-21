package cn.flowboot.simple.mapper;

import cn.flowboot.simple.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author by Vincent Vic
 * @since 2022-01-20
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}
