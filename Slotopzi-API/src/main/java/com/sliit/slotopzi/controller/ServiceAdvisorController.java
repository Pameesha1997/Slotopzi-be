package com.sliit.slotopzi.controller;

import com.sliit.slotopzi.dto.request.*;
import com.sliit.slotopzi.dto.response.GetCustomerDetailsRespond;
import com.sliit.slotopzi.dto.response.OngoingRepairResponse;
import com.sliit.slotopzi.dto.response.UpcomingAppointmentResponse;
import com.sliit.slotopzi.dto.response.VehicleDetailsAutofillResponse;
import com.sliit.slotopzi.model.Slot;
import com.sliit.slotopzi.service.ServiceAdvisorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/advisor")
public class ServiceAdvisorController {

    @Autowired
    ServiceAdvisorService serviceAdvisorService;

    // auto fill vehicle details on the ve
    @GetMapping("/getvehicle/{vin}")
    public ResponseEntity getVehicleDetails(@PathVariable String vin) {
        if(serviceAdvisorService.checkIfVehicleExists(vin)){
            VehicleDetailsAutofillResponse response = serviceAdvisorService.autoFillVehicleDetails(vin);
            return ResponseEntity.ok().body(response);
        }else if(serviceAdvisorService.checkIfVehicleExistsOnVehicleNum(vin)){
            VehicleDetailsAutofillResponse response = serviceAdvisorService.autoFillVehicleDetails(vin);
            return ResponseEntity.ok().body(response);
        }else
        return ResponseEntity.badRequest().body("Vehicle Not Exists");

    }

    // add new vehicle which not exists
    // Customer contact number should pass
    @PostMapping("/vehicle/add new")
    public ResponseEntity addNewVehicle(@RequestBody AddVehicleRequest addVehicleRequest) {
        if(serviceAdvisorService.checkIfVehicleExists(addVehicleRequest.getVin())){
           return ResponseEntity.badRequest().body("Vehicle Already Exists");
        }else {
            serviceAdvisorService.registerNewVehicle(addVehicleRequest);
            return ResponseEntity.ok().body("vehicle Added Successfully");
        }
    }
    @PutMapping("/vehicle/update")
    public ResponseEntity updateVehicle(@RequestBody AddVehicleRequest addVehicleRequest) {
        serviceAdvisorService.updateVehicle(addVehicleRequest);
        return ResponseEntity.ok().body("vehicle Updated Successfully");
    }
    @GetMapping("/getCustomer/{contactNo}")
    public ResponseEntity getCustomerDetails(@PathVariable String contactNo){
        GetCustomerDetailsRespond respond=serviceAdvisorService.autoFillCustomerDetails(contactNo);
        if(respond!=null){
            return ResponseEntity.ok().body(respond);

        }
        return ResponseEntity.badRequest().body("Add Customer");
    }

    //add new sketchy account for customer
    // it doesnt check for existing contact since it was filtered early. So, add non existing contact
    @PostMapping("/customer/addNew")
    public ResponseEntity addNewCustomerSketchy(@RequestBody AddSketchyCustomerRequest addSketchyCustomerRequest){
        serviceAdvisorService.addNewCustomerSketchy(addSketchyCustomerRequest);
        return ResponseEntity.ok().body("customer added Successfully");
    }

    @PostMapping("/addRepair")
    public ResponseEntity addNewRepair(@RequestBody AddNewRepairsRequest addNewRepairsRequest){
        long repairId=serviceAdvisorService.addNewRepair(addNewRepairsRequest);

        return ResponseEntity.ok().body(repairId);
    }

    @PostMapping("/add service entries")
    public ResponseEntity addServiceEntries(@RequestBody AddNewServiceEntryRequest addNewServiceEntryRequest){
        serviceAdvisorService.addNewServiceEntry(addNewServiceEntryRequest);
        return ResponseEntity.ok().body("Added to the DB");
    }

    @GetMapping("/getSubCategories/{secId}")
    public ResponseEntity getSubCategories(@PathVariable int secId){
        String section;
        switch (secId){
            case 1:
                section="General Repair";
                break;
            case 2:
                section="Wheel Alignment";
                break;
            case 3:
                section="Service";
                break;
            case 4:
                section="Express Maintainance";
                break;
            case 5:
                section="Washing";
                break;
            default:
                return ResponseEntity.badRequest().body("invalid Selection");
        }
        return ResponseEntity.ok().body(serviceAdvisorService.getSubCatList(section));
    }

    @GetMapping("/nextslot/{repairId}")
    public ResponseEntity setNextSlot(@PathVariable long repairId){
        Slot slot=serviceAdvisorService.getNextSlot(repairId);
        if(slot==null){
            return ResponseEntity.badRequest().body("All Repairs Completed");
        }
        return ResponseEntity.ok().body(slot);
    }
    //Get not handed over vehicles
    @GetMapping("/repairs/ongoing/{userId}")
    public ResponseEntity getOngoingRepairs(@PathVariable long userId){
        long advisorId=serviceAdvisorService.getStaffId(userId);
        if(serviceAdvisorService.checkIfAdvisorExists(advisorId)){
            List<OngoingRepairResponse> ongoingRepairResponses=serviceAdvisorService.getOngoingRepairList(advisorId);
            if(ongoingRepairResponses.isEmpty()){
                return ResponseEntity.badRequest().body("No ongoing repairs !");
            }else {
                return ResponseEntity.ok().body(ongoingRepairResponses);
            }
        }else
        return ResponseEntity.badRequest().body("Advisor Not Exists");
    }

    @GetMapping("/appointments/today/{userId}")
    public ResponseEntity getUpcomingAppointments(@PathVariable long userId){
        long advisorId=serviceAdvisorService.getStaffId(userId);
        if(serviceAdvisorService.checkIfAdvisorExists(advisorId)){
            List<UpcomingAppointmentResponse> upcomingAppointmentResponses=serviceAdvisorService.getPendingAppointments(advisorId);
            if(upcomingAppointmentResponses.isEmpty()){
                return ResponseEntity.badRequest().body("No Upcoming Appointments !");
            }else {
                return ResponseEntity.ok().body(upcomingAppointmentResponses);
            }
        }else
            return ResponseEntity.badRequest().body("Advisor Not Exists");
    }

    @PutMapping("addchecklist")
    public ResponseEntity addCheckList(@RequestBody ChecklistRequest checklistRequest){
        if(serviceAdvisorService.updateChecklist(checklistRequest))
            return ResponseEntity.ok().body("Checklist Submitted!!");
        else
            return ResponseEntity.badRequest().body("Error Occurred!! Invalid Repair!");
    }

    @GetMapping("getemailbypairid/{repairid}")
    public ResponseEntity getemailbyrepid(@PathVariable long repairid){
        String email= serviceAdvisorService.getemail(repairid);
        if(email!=null)
            return ResponseEntity.ok().body(email);
        else
            return ResponseEntity.badRequest().body("Error Occurred!! Invalid Repair!");
    }

    @GetMapping("/getAllRepairs/{userId}")
    public ResponseEntity getAllRepairs(@PathVariable long userId){
        long advisorId=serviceAdvisorService.getStaffId(userId);
        if(serviceAdvisorService.checkIfAdvisorExists(advisorId)){
            return ResponseEntity.ok().body(serviceAdvisorService.getAllRepairs(advisorId));
        }
        return ResponseEntity.badRequest().body("Advisor Not exists");
    }

}
