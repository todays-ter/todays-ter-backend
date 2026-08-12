package com.umc.todayter.global.config;

import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PlaceSeedTransactionService {

    private final PlaceRepository placeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void seedPlace(Place seedPlace) {
        placeRepository.findByName(seedPlace.getName())
                .ifPresentOrElse(
                        existingPlace -> syncExistingPlace(seedPlace, existingPlace),
                        () -> insertPlace(seedPlace)
                );
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByName(String name) {
        return placeRepository.findByName(name).isPresent();
    }

    private void syncExistingPlace(Place seedPlace, Place existingPlace) {
        try {
            boolean updated = false;
            if (StringUtils.hasText(seedPlace.getMapUrl())) {
                existingPlace.updateMapUrl(seedPlace.getMapUrl());
                updated = true;
            }
            if (StringUtils.hasText(seedPlace.getGooglePlaceId())) {
                existingPlace.updateGooglePlaceId(seedPlace.getGooglePlaceId());
                updated = true;
            }
            if (updated) {
                placeRepository.flush();
            }
        } catch (DataIntegrityViolationException e) {
            throw new PlaceSeedDataIntegrityException(
                    seedPlace.getName(),
                    PlaceSeedDataIntegrityException.Operation.UPDATE,
                    e
            );
        }
    }

    private void insertPlace(Place seedPlace) {
        try {
            placeRepository.saveAndFlush(seedPlace);
        } catch (DataIntegrityViolationException e) {
            throw new PlaceSeedDataIntegrityException(
                    seedPlace.getName(),
                    PlaceSeedDataIntegrityException.Operation.INSERT,
                    e
            );
        }
    }
}
