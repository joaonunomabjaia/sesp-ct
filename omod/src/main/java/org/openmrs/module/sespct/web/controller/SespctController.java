package org.openmrs.module.sespct.web.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.sespct.api.service.PedidoService;
import org.openmrs.module.sespct.api.model.Pedido;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/module/sespct/")
public class SespctController {
	
	private static final Logger log = LoggerFactory.getLogger(SespctController.class);
	
	@RequestMapping(value = "sespct.form", method = RequestMethod.GET)
	public String showMainPage(ModelMap model, @RequestParam(value = "estado", required = false) String estado) {
		
		try {
			PedidoService pedidoService = Context.getService(PedidoService.class);
			
			List<Pedido> requests;
			if (estado != null && !estado.trim().isEmpty()) {
				requests = pedidoService.getPedidosByEstado(estado);
			} else {
				requests = pedidoService.getAllPedidos();
			}
			
			model.addAttribute("pedidos", requests);
			model.addAttribute("selectedEstado", estado);
			
			log.info("Loaded " + requests.size() + " SESP-CT requests for display");
			
		}
		catch (Exception e) {
			log.error("Error loading SESP-CT requests", e);
			model.addAttribute("errorMessage", "Error loading data: " + e.getMessage());
		}
		
		return "/module/sespct/sespct/index";
	}
	
	/**
	 * Export functionality
	 */
	@RequestMapping(value = "/module/sespct/manageftcases/export", method = RequestMethod.GET)
	public String exportCases(ModelMap model) {
		// TODO: Implement export functionality
		return "redirect:/module/sespct/sespct.form";
	}
	
	/**
	 * View individual request details
	 */
	@RequestMapping(value = "/module/sespct/viewRequest", method = RequestMethod.GET)
	public String viewRequest(@RequestParam("pedidoId") String pedidoId, ModelMap model) {
		
		try {
			PedidoService pedidoService = Context.getService(PedidoService.class);
			Pedido request = pedidoService.getPedidoByExternalId(pedidoId);
			
			if (request != null) {
				model.addAttribute("pedido", request);
				return "/module/sespct/viewRequest";
			} else {
				model.addAttribute("errorMessage", "Request not found: " + pedidoId);
				return "redirect:/module/sespct/sespct.form";
			}
			
		}
		catch (Exception e) {
			log.error("Error loading SESP-CT request: " + pedidoId, e);
			model.addAttribute("errorMessage", "Error loading request: " + e.getMessage());
			return "redirect:/module/sespct/sespct.form";
		}
	}
	
	@RequestMapping(value = "manageftcases/export.form", method = RequestMethod.GET)
	public void downloadExcel(HttpServletResponse response) throws IOException {
		
		try {
			String content = "US,NID,NCFT\nUS Maputo,123456,NCFT001\n";
			
			response.setContentType("text/csv");
			response.setHeader("Content-Disposition", "attachment; filename=test.csv");
			
			response.getWriter().write(content);
			response.getWriter().flush();
			
		}
		catch (Exception e) {
			log.error("Error", e);
		}
	}
	
	@RequestMapping(value = "test", method = RequestMethod.GET)
	public String test() {
		return "sespct/index";
	}
}
