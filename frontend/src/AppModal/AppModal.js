import { Modal, Button } from 'react-bootstrap';
import React from 'react';

const AppModal = props => {
    return (<div>
        <Modal show={props.show} onHide={props.handleClose}>
            <Modal.Header closeButton>
                <Modal.Title>{props.response.title}</Modal.Title>
            </Modal.Header>
            <Modal.Body>{props.response.body}</Modal.Body>
            <Modal.Footer>
                <Button variant="primary" onClick={props.handleClose}>
                    OK
                </Button>
            </Modal.Footer>
        </Modal>
    </div>);
}

export default AppModal;