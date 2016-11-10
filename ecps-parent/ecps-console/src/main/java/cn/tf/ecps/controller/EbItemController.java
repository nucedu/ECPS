package cn.tf.ecps.controller;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.tf.ecps.po.EbBrand;
import cn.tf.ecps.po.EbFeature;
import cn.tf.ecps.po.EbItem;
import cn.tf.ecps.po.EbItemClob;
import cn.tf.ecps.po.EbParaValue;
import cn.tf.ecps.po.EbSku;
import cn.tf.ecps.po.EbSpecValue;
import cn.tf.ecps.service.EbBrandService;
import cn.tf.ecps.service.EbFeatureService;
import cn.tf.ecps.service.EbItemService;
import cn.tf.ecps.utils.Page;
import cn.tf.ecps.utils.QueryCondition;

@Controller
@RequestMapping("/item")
public class EbItemController {

	@Autowired
	private EbBrandService brandService;

	@Autowired
	private EbItemService itemService;

	@Autowired
	private EbFeatureService featureService;
	
	
	@RequestMapping("/toIndex.do")
	public String toIndex() {
		return "item/index";
	}

	/**
	 * 查询所有的品牌
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/selectBrandAll.do")
	public String selectBrandAll(Model model) {
		List<EbBrand> bList = brandService.selectBrandAll();
		model.addAttribute("bList", bList);
		return "item/listbrand";
	}

	/**
	 * 跳转到添加品牌的页面
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/toAddBrand.do")
	public String toAddBrand(Model model) {
		return "item/addbrand";
	}

	/**
	 * 验证品牌名称的重复性
	 * 
	 * @param brandName
	 * @param out
	 */
	@RequestMapping("/validBrandName.do")
	public void validBrandName(String brandName, PrintWriter out) {
		List<EbBrand> brandList = brandService.validBrandName(brandName);
		String flag = "yes";
		if (brandList.size() > 0) {
			flag = "no";
		}
		out.write(flag);

	}

	@RequestMapping("/addBrand.do")
	public String addBrand(EbBrand brand) {
		brandService.saveBrand(brand);
		return "redirect:selectBrandAll.do";
	}

	// 删除品牌
	@RequestMapping("/delete.do")
	public String delete(String id) {
		int i = brandService.deleteBrand(id);
		if (i > 0) {
			return "redirect:selectBrandAll.do";
		}
		return null;
	}

	// 删除商品
	@RequestMapping("/deleteGoods.do")
	public String deleteGoods(String id) {
		int i = itemService.deleteGoods(id);
		
		if (i > 0) {
			return "redirect:listItem.do";
		}
		return null;
	}

	@RequestMapping("/listItem.do")
	public String listItem(QueryCondition qc, Model model) {
		List<EbBrand> bList = brandService.selectBrandAll();
		model.addAttribute("bList", bList);
		if (qc.getPageNo() == null) {
			qc.setPageNo(1);
		}

		Page page = itemService.selectItemByCondition(qc);
		model.addAttribute("page", page);
		model.addAttribute("qc", qc); // 把查询条件回显
		return "item/list";
	}
	
	//查看商品
	
	@RequestMapping("/viewItem.do")
	public String toviewItem(String id,Model model) {
		System.out.println(id);
		EbItem  ebItem= itemService.selectItemByNo(id);
		System.out.println(ebItem);
		model.addAttribute("ebItem",ebItem);
		return "item/viewItem";
	}
	//到添加商品的页面
	@RequestMapping("/toAddItem.do")
	public String toAddItem(Model model) {
		List<EbBrand> bList = brandService.selectBrandAll();
		model.addAttribute("bList", bList);
		
		List<EbFeature> commlist = featureService.selectCommFeature();
		model.addAttribute("commList",commlist);
		
		List<EbFeature> speclist = featureService.selectSpecFeature();
		model.addAttribute("specList",speclist);
		return "item/addItem";
	}
	
	//添加商品
	@RequestMapping("/addItem.do")
	public String addItem(EbItem item,EbItemClob itemClob,HttpServletRequest request,Integer divNum){
		List<EbFeature> commList = featureService.selectCommFeature();
		List<EbParaValue> paraList=new ArrayList<EbParaValue>();
		
		
		for (EbFeature ebFeature : commList) {
			Long featureId = ebFeature.getFeatureId();
			if(ebFeature.getInputType()==3){
				String[] paraArr = request.getParameterValues(featureId+"");
				
				//多选情况使用逗号连接起来
				if(paraArr!=null && paraArr.length>0){
					String paraValString="";
					for(String para:paraArr){
						paraValString=paraValString+para+",";
					}
					paraValString=paraValString.substring(0,paraValString.length());
					EbParaValue ev=new EbParaValue();
					ev.setFeatureId(featureId);
					ev.setParaValue(paraValString);
					paraList.add(ev);
				}
				
			
			
			}else{
				String para=request.getParameter(featureId+"");
				if(StringUtils.isNotBlank(para) && !StringUtils.equals(para, "")){
					EbParaValue ev = new EbParaValue();
					ev.setFeatureId(featureId);
					ev.setParaValue(para);
					paraList.add(ev);
				}

			}
			
		}
		List<EbSku> skuList = new ArrayList<EbSku>();
		List<EbFeature> specList = featureService.selectSpecFeature();
		
		//循环div的个数，在循环中获取每个最小单元的值
		for(int i=1;i<=divNum;i++){
			String skuPrice = request.getParameter("skuPrice"+i);
			String stockInventory = request.getParameter("stockInventory"+i);
			//判断divNum编号没有断档的情况，因为skuPrice和stockInventory是必填字段
			if(StringUtils.isNotBlank(skuPrice) && StringUtils.isNotBlank(stockInventory)){
				String sort = request.getParameter("sort"+i);
				String skuType = request.getParameter("skuType"+i);
				String showStatus = request.getParameter("showStatus"+i);
				String marketPrice = request.getParameter("marketPrice"+i);
				String skuUpperLimit = request.getParameter("skuUpperLimit"+i);
				String sku = request.getParameter("sku"+i);
				String location = request.getParameter("location"+i);
				//创建sku对象
				EbSku skuObj = new EbSku();
				if(StringUtils.isNotBlank(sort)){
					skuObj.setSkuSort(new Integer(sort));
				}
				skuObj.setSkuPrice(new BigDecimal(skuPrice));
				skuObj.setStockInventory(new Integer(stockInventory));
				if(StringUtils.isNotBlank(skuType)){
					skuObj.setSkuType(new Short(skuType));
				}
				if(StringUtils.isNotBlank(showStatus)){
					skuObj.setShowStatus(new Short(showStatus));
				}
				if(StringUtils.isNotBlank(marketPrice)){
					skuObj.setMarketPrice(new BigDecimal(marketPrice));
				}
				if(StringUtils.isNotBlank(skuUpperLimit)){
					skuObj.setSkuUpperLimit(new Integer(skuUpperLimit));
				}
				skuObj.setSku(sku);
				skuObj.setLocation(location);
				
				List<EbSpecValue> svList= new ArrayList<EbSpecValue>();
				for(EbFeature feature:specList){
					//获得特殊属性的Id
					Long featureId = feature.getFeatureId();
					//获得当前最小销售单元的每一个特殊属性的值
					String specVal = request.getParameter(featureId+"specradio"+i);
					EbSpecValue sv = new EbSpecValue();
					sv.setFeatureId(featureId);
					sv.setSpecValue(specVal);
					svList.add(sv);
				}
				skuObj.setSpecList(svList);
				skuList.add(skuObj);
			}
		}
		
		//itemService.saveItem(item, itemClob, paraList, skuList);
		return "redirect:listItem.do?showStatus=1";
	}


}