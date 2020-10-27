package org.isf.vactype.rest;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.isf.shared.exceptions.OHResponseEntityExceptionHandler;
import org.isf.shared.mapper.converter.BlobToByteArrayConverter;
import org.isf.shared.mapper.converter.ByteArrayToBlobConverter;
import org.isf.vactype.data.VaccineTypeHelper;
import org.isf.vactype.dto.VaccineTypeDTO;
import org.isf.vactype.manager.VaccineTypeBrowserManager;
import org.isf.vactype.mapper.VaccineTypeMapper;
import org.isf.vactype.model.VaccineType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VaccineTypeControllerTest {
	private final Logger logger = LoggerFactory.getLogger(VaccineTypeControllerTest.class);
	
	@Mock
	protected VaccineTypeBrowserManager vaccineTypeBrowserManagerMock;
	
	protected VaccineTypeMapper vaccineTypeMapper = new VaccineTypeMapper();
	
	private MockMvc mockMvc;
		
	@Before
    public void setup() {
    	MockitoAnnotations.initMocks(this);
    	this.mockMvc = MockMvcBuilders
				.standaloneSetup(new VaccineTypeController(vaccineTypeBrowserManagerMock, vaccineTypeMapper))
   				.setControllerAdvice(new OHResponseEntityExceptionHandler())
   				.build();
    	ModelMapper modelMapper = new ModelMapper();
		modelMapper.addConverter(new BlobToByteArrayConverter());
		modelMapper.addConverter(new ByteArrayToBlobConverter());
		ReflectionTestUtils.setField(vaccineTypeMapper, "modelMapper", modelMapper);
    }

	@Test
	public void testGetVaccineType_200() throws JsonProcessingException, Exception {
		String request = "/vaccinetype";
		
		ArrayList<VaccineType> vaccinesTypeList = VaccineTypeHelper.setupVaccineList(4);

		when(vaccineTypeBrowserManagerMock.getVaccineType())
			.thenReturn(vaccinesTypeList);
		
		List<VaccineTypeDTO> expectedVaccineTypeDTOs = vaccineTypeMapper.map2DTOList(vaccinesTypeList);
		
		MvcResult result = this.mockMvc
			.perform(get(request))
			.andDo(log())
			.andExpect(status().is2xxSuccessful())
			.andExpect(status().isOk())	
			.andExpect(content().string(containsString(new ObjectMapper().writeValueAsString(expectedVaccineTypeDTOs))))
			.andReturn();
		
		logger.debug("result: {}", result);
	}

	@Test
	public void testNewVaccineType_200() throws Exception {
		String request = "/vaccinetype";
		String code = "ZZ";
		VaccineTypeDTO  body = vaccineTypeMapper.map2DTO(VaccineTypeHelper.setup(code));
		
		boolean isCreated = true;
		when(vaccineTypeBrowserManagerMock.newVaccineType(vaccineTypeMapper.map2Model(body)))
			.thenReturn(isCreated);
		
		MvcResult result = this.mockMvc
			.perform(post(request)
					.contentType(MediaType.APPLICATION_JSON)
					.content(VaccineTypeHelper.asJsonString(body))
					)
			.andDo(log())
			.andExpect(status().is2xxSuccessful())
			.andExpect(status().isCreated())	
			.andReturn();
		
		logger.debug("result: {}", result);
	}

	@Test
	public void testUpdateVaccineType_200() throws Exception {
		String request = "/vaccinetype";
		String code = "ZZ";
		VaccineTypeDTO  body = vaccineTypeMapper.map2DTO(VaccineTypeHelper.setup(code));

		boolean isUpdated = true;
		when(vaccineTypeBrowserManagerMock.updateVaccineType(vaccineTypeMapper.map2Model(body)))
			.thenReturn(isUpdated);
		
		MvcResult result = this.mockMvc
			.perform(put(request)
					.contentType(MediaType.APPLICATION_JSON)
					.content(VaccineTypeHelper.asJsonString(body))
					)
			.andDo(log())
			.andExpect(status().is2xxSuccessful())
			.andExpect(status().isOk())	
			.andReturn();
		
		logger.debug("result: {}", result);
	}

	@Test
	public void testDeleteVaccineType_200() throws Exception {
		String request = "/vaccinetype/{code}";
		String basecode = "0";
		
		VaccineType vaccineType = VaccineTypeHelper.setup(basecode);
		VaccineTypeDTO  body = vaccineTypeMapper.map2DTO(vaccineType);
		String code = body.getCode();
		
		when(vaccineTypeBrowserManagerMock.findVaccineType(code))
			.thenReturn(vaccineType);

		when(vaccineTypeBrowserManagerMock.deleteVaccineType(vaccineTypeMapper.map2Model(body)))
			.thenReturn(true);
		
		String isDeleted = "true";
		MvcResult result = this.mockMvc
			.perform(delete(request, code))
			.andDo(log())
			.andExpect(status().is2xxSuccessful())
			.andExpect(status().isOk())	
			.andExpect(content().string(containsString(isDeleted)))
			.andReturn();
		
		logger.debug("result: {}", result);
	}

	@Test
	public void testCheckVaccineTypeCode_200() throws Exception {
	String request = "/vaccinetype/check/{code}";
		
		String code = "AA";
		VaccineType vaccineType = VaccineTypeHelper.setup(code);

		when(vaccineTypeBrowserManagerMock.codeControl(vaccineType.getCode()))
			.thenReturn(true);
		
		MvcResult result = this.mockMvc
			.perform(get(request, code))
			.andDo(log())
			.andExpect(status().is2xxSuccessful())
			.andExpect(status().isOk())	
			.andExpect(content().string("true"))
			.andReturn();
		
		logger.debug("result: {}", result);	
	}

}
