package com.platform.hr.branch.mapper;

import com.platform.hr.branch.dto.BranchDto;
import com.platform.hr.branch.entity.Branch;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-10T17:13:18+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class BranchMapperImpl implements BranchMapper {

    @Override
    public Branch toEntity(BranchDto.CreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Branch.BranchBuilder branch = Branch.builder();

        branch.name( request.getName() );
        branch.code( request.getCode() );
        branch.address( request.getAddress() );
        branch.city( request.getCity() );
        branch.country( request.getCountry() );
        branch.phone( request.getPhone() );
        branch.email( request.getEmail() );
        branch.headOffice( request.isHeadOffice() );

        return branch.build();
    }

    @Override
    public BranchDto.Response toResponse(Branch branch) {
        if ( branch == null ) {
            return null;
        }

        BranchDto.Response response = new BranchDto.Response();

        response.setId( branch.getId() );
        response.setName( branch.getName() );
        response.setCode( branch.getCode() );
        response.setAddress( branch.getAddress() );
        response.setCity( branch.getCity() );
        response.setCountry( branch.getCountry() );
        response.setPhone( branch.getPhone() );
        response.setEmail( branch.getEmail() );
        response.setHeadOffice( branch.isHeadOffice() );
        response.setCreatedAt( branch.getCreatedAt() );

        return response;
    }

    @Override
    public BranchDto.Summary toSummary(Branch branch) {
        if ( branch == null ) {
            return null;
        }

        BranchDto.Summary summary = new BranchDto.Summary();

        summary.setId( branch.getId() );
        summary.setName( branch.getName() );
        summary.setCode( branch.getCode() );
        summary.setCity( branch.getCity() );
        summary.setCountry( branch.getCountry() );

        return summary;
    }

    @Override
    public void updateEntity(BranchDto.UpdateRequest request, Branch branch) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            branch.setName( request.getName() );
        }
        if ( request.getAddress() != null ) {
            branch.setAddress( request.getAddress() );
        }
        if ( request.getCity() != null ) {
            branch.setCity( request.getCity() );
        }
        if ( request.getCountry() != null ) {
            branch.setCountry( request.getCountry() );
        }
        if ( request.getPhone() != null ) {
            branch.setPhone( request.getPhone() );
        }
        if ( request.getEmail() != null ) {
            branch.setEmail( request.getEmail() );
        }
        if ( request.getHeadOffice() != null ) {
            branch.setHeadOffice( request.getHeadOffice() );
        }
    }
}
