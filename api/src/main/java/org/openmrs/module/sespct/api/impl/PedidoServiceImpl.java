package org.openmrs.module.sespct.api.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.sespct.api.service.PedidoService;
import org.openmrs.module.sespct.api.dao.PedidoDao;
import org.openmrs.module.sespct.api.model.Pedido;
import org.openmrs.module.sespct.api.model.HistoriaTarv;
import org.openmrs.module.sespct.api.model.DadosLaboratorioCD4;
import org.openmrs.module.sespct.api.model.DadosLaboratorioCargaViral;
import org.openmrs.module.sespct.config.CTConfig;
import org.openmrs.module.sespct.ct.CtClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Transactional
public class PedidoServiceImpl extends BaseOpenmrsService implements PedidoService {
	
	private static final Log log = LogFactory.getLog(PedidoServiceImpl.class);
	
	@Autowired
	private PedidoDao pedidoDao;
	
	@Autowired
	private CtClient ctClient;
	
	@Autowired
	private CTConfig cfg;
	
	public void setPedidoDao(PedidoDao pedidoDao) {
		this.pedidoDao = pedidoDao;
	}
	
	@Override
	public void initializeModule() {
		log.info("Initializing SESP-CT Module...");
		List<Pedido> existingPedidos = pedidoDao.getAllPedidos();
		if (existingPedidos.isEmpty() || existingPedidos.size() > 20) {
			log.info("No existing data found. Creating dummy data...");
			createDummyData();
		} else {
			log.info("Found " + existingPedidos.size() + " existing pedidos. Skipping dummy data creation.");
		}
	}
	
	@Override
	public Pedido savePedido(Pedido pedido) {
		return pedidoDao.savePedido(pedido);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Pedido getPedidoById(Integer id) {
		return pedidoDao.getPedidoById(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Pedido getPedidoByExternalId(String externalId) {
		return pedidoDao.getPedidoByExternalId(externalId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Pedido> getAllPedidos() {
		return pedidoDao.getAllPedidos();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Pedido> getPedidosByEstado(String estado) {
		return pedidoDao.getPedidosByEstado(estado);
	}
	
	@Override
	public void deletePedido(Pedido pedido) {
		pedidoDao.deletePedido(pedido);
	}
	
	@Override
	public void createDummyData() {
		try {
			log.info("Creating dummy SESP-CT data...");
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			
			for (int i = 1; i <= 50; i++) {
				Pedido pedido = new Pedido();
				
				// Set standard OpenMRS fields
				pedido.setUuid(UUID.randomUUID().toString());
				pedido.setCreator(Context.getAuthenticatedUser());
				pedido.setDateCreated(new Date());
				
				// Metadata with incremental pedidoId and slight changes
				pedido.setPedidoId("15" + (1500 + i)); // e.g., 15151, 15152...
				pedido.setDataSubmissao(dateTimeFormat.parse("2024-03-15T10:30:00Z"));
				pedido.setVersao("2.0");
				pedido.setOrigem("SESP_MOZAMBIQUE");
				pedido.setTipoFormulario("e-FT");
				pedido.setSolicitadoPor("user_sesp_" + (100 + i));
				
				// Alternate estado and causa for some records for GCT_BR3 mapping
				if (i % 10 == 0) { // every 10th record
					pedido.setEstado("Não Processado");
					pedido.setCausa("NID não encontrado");
				} else {
					pedido.setEstado("Sem resposta");
					pedido.setCausa(null);
				}
				
				// DadosUtente (increment name, nid, initials, idade, etc.)
				pedido.getDadosUtente().setNomeCompleto("João Manuel Santos " + i);
				pedido.getDadosUtente().setIniciais("JMS" + i);
				pedido.getDadosUtente().setNid("0109010701/2007/00" + String.format("%03d", i));
				pedido.getDadosUtente().setIdade(30.0 + i * 0.5); // 30, 30.5, 31...
				pedido.getDadosUtente().setEstadioOms("Estadio III");
				pedido.getDadosUtente().setEstadioOmsMotivo("Perda de peso significativa e infecções recorrentes");
				pedido.getDadosUtente().setProvincia("Maputo");
				pedido.getDadosUtente().setDistrito("Maputo Cidade");
				pedido.getDadosUtente().setUnidadeSanitaria("Hospital Central de Maputo");
				pedido.getDadosUtente().setCodigoUnidadeSanitaria("HCM" + String.format("%03d", i));
				pedido.getDadosUtente().setPeso(60.0 + i * 0.5);
				pedido.getDadosUtente().setSexo(i % 2 == 0 ? "masculino" : "feminino");
				pedido.getDadosUtente().setGestante("nao");
				pedido.getDadosUtente().setDataProvavelParto(null);
				pedido.getDadosUtente().setLactante("nao");
				pedido.getDadosUtente().setDataParto(null);
				
				// ReportarFalencia
				pedido.getReportarFalencia()
				        .setHistoriaClinica(
				            "Paciente com histórico de boa adesão inicial, apresentando sinais de falência terapêutica nos últimos 6 meses. Observado perda de peso progressiva e aumento de infecções oportunistas.");
				pedido.getReportarFalencia()
				        .setHistoriaAdesao(
				            "Adesão inicial excelente (>95%) nos primeiros 2 anos. Declínio gradual observado a partir de 2022 devido a fatores socioeconômicos. Adesão atual estimada em 70-80%.");
				pedido.getReportarFalencia().setTratamentoTbAtivo("nao");
				
				// DadosClinico
				pedido.getDadosClinico().setNome("Dr. Maria Santos");
				pedido.getDadosClinico().setCategoriaProfissional("Medico");
				pedido.getDadosClinico().setTelefone("+258823456789");
				pedido.getDadosClinico().setEmail("maria.santos@misau.gov.mz");
				
				// LinhaSolicitada
				pedido.getLinhaSolicitada().setLinha("2 Linha");
				pedido.getLinhaSolicitada().setAnexo("[BASE64_ENCODED_ATTACHMENT]");
				
				// Save Pedido first (to get id if needed)
				pedido = pedidoDao.savePedido(pedido);
				
				// Create and add TARV History
				HistoriaTarv tarv1 = new HistoriaTarv();
				tarv1.setUuid(UUID.randomUUID().toString());
				tarv1.setCreator(Context.getAuthenticatedUser());
				tarv1.setDateCreated(new Date());
				tarv1.setPedido(pedido);
				tarv1.setDataInicio(dateFormat.parse("2020-01-15"));
				tarv1.setDataTermino(dateFormat.parse("2022-06-30"));
				tarv1.setEsquemaTarv("AZT+3TC+EFV");
				
				HistoriaTarv tarv2 = new HistoriaTarv();
				tarv2.setUuid(UUID.randomUUID().toString());
				tarv2.setCreator(Context.getAuthenticatedUser());
				tarv2.setDateCreated(new Date());
				tarv2.setPedido(pedido);
				tarv2.setDataInicio(dateFormat.parse("2022-07-01"));
				tarv2.setDataTermino(dateFormat.parse("2024-03-10"));
				tarv2.setEsquemaTarv("TDF+3TC+EFV");
				
				pedido.getHistoriaTarv().add(tarv1);
				pedido.getHistoriaTarv().add(tarv2);
				
				// Create and add CD4 Data
				DadosLaboratorioCD4 cd4_1 = new DadosLaboratorioCD4();
				cd4_1.setUuid(UUID.randomUUID().toString());
				cd4_1.setCreator(Context.getAuthenticatedUser());
				cd4_1.setDateCreated(new Date());
				cd4_1.setPedido(pedido);
				cd4_1.setData(dateFormat.parse("2024-01-15"));
				cd4_1.setCd4(250 + i); // slight variation
				cd4_1.setCd4Percentagem(12.5 + i * 0.1);
				
				DadosLaboratorioCD4 cd4_2 = new DadosLaboratorioCD4();
				cd4_2.setUuid(UUID.randomUUID().toString());
				cd4_2.setCreator(Context.getAuthenticatedUser());
				cd4_2.setDateCreated(new Date());
				cd4_2.setPedido(pedido);
				cd4_2.setData(dateFormat.parse("2024-02-20"));
				cd4_2.setCd4(180 + i);
				cd4_2.setCd4Percentagem(8.2 + i * 0.1);
				
				pedido.getDadosLaboratorioCD4().add(cd4_1);
				pedido.getDadosLaboratorioCD4().add(cd4_2);
				
				// Create and add Viral Load Data
				DadosLaboratorioCargaViral vl1 = new DadosLaboratorioCargaViral();
				vl1.setUuid(UUID.randomUUID().toString());
				vl1.setCreator(Context.getAuthenticatedUser());
				vl1.setDateCreated(new Date());
				vl1.setPedido(pedido);
				vl1.setData(dateFormat.parse("2024-01-15"));
				vl1.setCargaViral(15000L + i * 100);
				
				DadosLaboratorioCargaViral vl2 = new DadosLaboratorioCargaViral();
				vl2.setUuid(UUID.randomUUID().toString());
				vl2.setCreator(Context.getAuthenticatedUser());
				vl2.setDateCreated(new Date());
				vl2.setPedido(pedido);
				vl2.setData(dateFormat.parse("2024-02-20"));
				vl2.setCargaViral(25000L + i * 100);
				
				pedido.getDadosLaboratorioCargaViral().add(vl1);
				pedido.getDadosLaboratorioCargaViral().add(vl2);
				
				// Save Pedido again to persist relationships
				pedidoDao.savePedido(pedido);
			}
			
			log.info("Dummy Pedido data created successfully");
			
		}
		catch (ParseException e) {
			log.error("Error parsing dates while creating dummy data", e);
		}
		catch (Exception e) {
			log.error("Error creating dummy Pedido data", e);
		}
	}

    private Pedido mapJsonToPedido(JsonNode dp) {
        Pedido pedido = new Pedido();
        pedido.setUuid(UUID.randomUUID().toString());
        pedido.setDateCreated(new Date());

        // Campos principais do Pedido
        pedido.setPedidoId(dp.path("pedidoId").asText(null));
        pedido.setSolicitadoPor(dp.path("solicitadoPor").asText(null));
        pedido.setEstado(dp.path("estado").asText("Sem resposta"));
        pedido.setCausa(dp.path("causa").asText(null));
        pedido.setVersao(dp.path("versao").asText(null));
        pedido.setOrigem(dp.path("origem").asText(null));
        pedido.setTipoFormulario(dp.path("tipoFormulario").asText(null));

        // Data de submissão
        String dataSubmissaoStr = dp.path("dataSubmissao").asText(null);
        if (dataSubmissaoStr != null) {
            try {
                pedido.setDataSubmissao(javax.xml.bind.DatatypeConverter.parseDateTime(dataSubmissaoStr).getTime());
            } catch (Exception e) {
                log.warn("Erro ao parsear dataSubmissao: " + dataSubmissaoStr, e);
            }
        }

        // DadosUtente
        JsonNode du = dp.path("dadosUtente");
        if (!du.isMissingNode()) {
            pedido.getDadosUtente().setNomeCompleto(du.path("nomeCompleto").asText(null));
            pedido.getDadosUtente().setIniciais(du.path("iniciais").asText(null));
            pedido.getDadosUtente().setNid(du.path("nid").asText(null));
            pedido.getDadosUtente().setIdade(du.path("idade").asDouble(0));
            pedido.getDadosUtente().setSexo(du.path("sexo").asText(null));
            pedido.getDadosUtente().setProvincia(du.path("provincia").asText(null));
            pedido.getDadosUtente().setDistrito(du.path("distrito").asText(null));
            pedido.getDadosUtente().setUnidadeSanitaria(du.path("unidadeSanitaria").asText(null));
            pedido.getDadosUtente().setCodigoUnidadeSanitaria(du.path("codigoUnidadeSanitaria").asText(null));
            pedido.getDadosUtente().setPeso(du.path("peso").asDouble(0));
            pedido.getDadosUtente().setGestante(du.path("gestante").asText(null));
            pedido.getDadosUtente().setDataProvavelParto(du.path("dataProvavelParto").isTextual() ? parseDate(du.path("dataProvavelParto").asText()) : null);
            pedido.getDadosUtente().setLactante(du.path("lactante").asText(null));
            pedido.getDadosUtente().setDataParto(du.path("dataParto").isTextual() ? parseDate(du.path("dataParto").asText()) : null);
            pedido.getDadosUtente().setEstadioOms(du.path("estadioOms").asText(null));
            pedido.getDadosUtente().setEstadioOmsMotivo(du.path("estadioOmsMotivo").asText(null));
        }

        // ReportarFalencia
        JsonNode rf = dp.path("reportarFalencia");
        if (!rf.isMissingNode()) {
            pedido.getReportarFalencia().setHistoriaClinica(rf.path("historiaClinica").asText(null));
            pedido.getReportarFalencia().setHistoriaAdesao(rf.path("historiaAdesao").asText(null));
            pedido.getReportarFalencia().setTratamentoTbAtivo(rf.path("tratamentoTbAtivo").asText(null));
        }

        // DadosClinico
        JsonNode dc = dp.path("dadosClinico");
        if (!dc.isMissingNode()) {
            pedido.getDadosClinico().setNome(dc.path("nome").asText(null));
            pedido.getDadosClinico().setCategoriaProfissional(dc.path("categoriaProfissional").asText(null));
            pedido.getDadosClinico().setTelefone(dc.path("telefone").asText(null));
            pedido.getDadosClinico().setEmail(dc.path("email").asText(null));
        }

        // LinhaSolicitada
        JsonNode ls = dp.path("linhaSolicitada");
        if (!ls.isMissingNode()) {
            pedido.getLinhaSolicitada().setLinha(ls.path("linha").asText(null));
            pedido.getLinhaSolicitada().setAnexo(ls.path("anexo").asText(null));
        }

        // HistoriaTarv (array)
        JsonNode hts = dp.path("historiaTarv");
        if (hts.isArray()) {
            for (JsonNode ht : hts) {
                HistoriaTarv historia = new HistoriaTarv();
                historia.setUuid(UUID.randomUUID().toString());
                historia.setCreator(Context.getAuthenticatedUser());
                historia.setDateCreated(new Date());
                historia.setPedido(pedido);
                historia.setDataInicio(parseDate(ht.path("dataInicio").asText(null)));
                historia.setDataTermino(parseDate(ht.path("dataTermino").asText(null)));
                historia.setEsquemaTarv(ht.path("esquemaTarv").asText(null));
                pedido.getHistoriaTarv().add(historia);
            }
        }

        // DadosLaboratorioCD4 (array)
        JsonNode cds = dp.path("dadosLaboratorioCD4");
        if (cds.isArray()) {
            for (JsonNode cd : cds) {
                DadosLaboratorioCD4 cd4 = new DadosLaboratorioCD4();
                cd4.setUuid(UUID.randomUUID().toString());
                cd4.setCreator(Context.getAuthenticatedUser());
                cd4.setDateCreated(new Date());
                cd4.setPedido(pedido);
                cd4.setData(parseDate(cd.path("data").asText(null)));
                cd4.setCd4(cd.path("cd4").asInt(0));
                cd4.setCd4Percentagem(cd.path("cd4Percentagem").asDouble(0));
                pedido.getDadosLaboratorioCD4().add(cd4);
            }
        }

        // DadosLaboratorioCargaViral (array)
        JsonNode vls = dp.path("dadosLaboratorioCargaViral");
        if (vls.isArray()) {
            for (JsonNode vl : vls) {
                DadosLaboratorioCargaViral carga = new DadosLaboratorioCargaViral();
                carga.setUuid(UUID.randomUUID().toString());
                carga.setCreator(Context.getAuthenticatedUser());
                carga.setDateCreated(new Date());
                carga.setPedido(pedido);
                carga.setData(parseDate(vl.path("data").asText(null)));
                carga.setCargaViral(vl.path("cargaViral").asLong(0));
                pedido.getDadosLaboratorioCargaViral().add(carga);
            }
        }

        return pedido;
    }

    // Helper para parse de datas
    private Date parseDate(String iso) {
        if (iso == null) return null;
        try {
            return javax.xml.bind.DatatypeConverter.parseDateTime(iso).getTime();
        } catch (Exception e) {
            log.warn("Erro ao parsear data ISO: " + iso, e);
            return null;
        }
    }

    @Override
    public Pedido saveFromJson(JsonNode dp) {
        if (dp == null || dp.isEmpty()) return null;
        return pedidoDao.saveOrFromJson(mapJsonToPedido(dp));
    }

    @Async("sespctTaskExecutor")
    @Override
    public void fetchAndCreateFromCtAsync(String requestId, String facilityCode) {
        try {
            String fac = (facilityCode != null && !facilityCode.isEmpty()) ? facilityCode : cfg.getDefaultFacility();
            JsonNode dp = ctClient.getPedidoById(requestId, fac);
            Pedido pedido = mapJsonToPedido(dp);
            pedidoDao.saveOrFromJson(pedido);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
