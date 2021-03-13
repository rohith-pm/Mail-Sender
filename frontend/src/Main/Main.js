import axios from "axios";
import React, { useRef, useState } from "react";
import { Form, Button, Card, FormFile, Spinner } from "react-bootstrap";
import AppModal from "../AppModal/AppModal";
import { AiOutlinePlusSquare } from "react-icons/ai";
import Attachment from "../Attachment/Attachment";

const Main = props => {
    const [state, setState] = useState({
        from: "",
        to: "",
        subject: "",
        body: "",
        count: 1,
        file: "",
        attachments: [],
        loading: false,
        response: "",
        showModal: false
    });

    const attachment = useRef(null);

    const triggerAttachmentUpload = () => {
        attachment.current.click();
    }

    const removeAttachment = (filename) => {
        let index = -1;
        let attachments = state.attachments;
        for (let i = 0; i < attachments.length; i++) {
            if (attachments[i].name === filename) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            attachments.splice(index, 1);
        }
        setState({
            ...state,
            attachments: attachments
        })
    }
    const updateState = (event, id) => {
        setState({
            ...state,
            [id]: event.target.value
        });
    }

    const setFile = (event, id) => {
        setState({
            ...state,
            [id]: event.target.files[0]
        })
    }

    const setAttachments = (event, id) => {
        let att = [...state.attachments];
        for (let i = 0; i < event.target.files.length; i++) {
            att.push(event.target.files[i]);
        }
        setState({
            ...state,
            attachments: att
        })
    }
    const sendEmails = () => {
        const formData = new FormData();
        formData.append('from', state.from);
        formData.append('to', state.to);
        formData.append('subject', state.subject);
        formData.append('mailBody', state.body);
        formData.append('count', state.count);
        formData.append('file', state.file);
        for (let i = 0; i < state.attachments.length; i++) {
            formData.append('attachments', state.attachments[i]);
        }
        setState({
            ...state,
            loading: true
        })
        axios.post("http://localhost:8085/send", formData)
            .then(res => {
                setState({
                    ...state,
                    loading: false,
                    showModal: true,
                    response: {
                        body: res.response != null ? res.response.data : "Mails sent",
                        title: "Successful"
                    }
                })
            })
            .catch(e => {
                console.log(e);
                setState({
                    ...state,
                    loading: false,
                    showModal: true,
                    response: {
                        body: e.response !== null ? JSON.stringify(e.response) : "Error",
                        title: "Something went wrong"
                    }
                })
            })
    }

    const handleClose = () => {
        setState({
            ...state,
            showModal: false
        })
    }
    let spinner = <Spinner animation="border" variant="primary" style={{ margin: 10 }} />;
    if (!state.loading)
        spinner = null;
    return (<div>
        <AppModal response={state.response} show={state.showModal} handleClose={handleClose} />
        <Card style={{ margin: 20, width: 900 }}>
            <Card.Body>
                <Form>
                    <div style={{
                        display: "flex",
                        flexDirection: "column",
                        justifyContent: "center",
                        alignContent: "center",
                        justifyItems: "center"
                    }}>
                        <Form.Group>
                            <Form.Label>From address</Form.Label>
                            <Form.Control required id="from" type="email" placeholder="Enter email" onChange={(e) => updateState(e, "from")} />
                        </Form.Group>
                        <Form.Group>
                            <Form.Label>To address</Form.Label>
                            <Form.Control required id="to" type="email" placeholder="Enter email" onChange={(e) => updateState(e, "to")} />
                        </Form.Group>
                        <Form.Group>
                            <Form.Label>Subject</Form.Label>
                            <Form.Control required id="subject" type="text" placeholder="No Subject" onChange={(e) => updateState(e, "subject")} />
                        </Form.Group>
                        <Form.Group>
                            <Form.Label>Body</Form.Label>
                            <Form.Control required style={{ height: 120 }} id="body" as="textarea" placeholder="No Body" onChange={(e) => updateState(e, "body")} />
                        </Form.Group>
                        <Form.Group>
                            <Form.Label>Number of Emails</Form.Label>
                            <Form.Control required id="count" type="number" defaultValue="1" onChange={(e) => updateState(e, "count")} />
                        </Form.Group>
                        <FormFile style={{ marginBottom: 15 }}>
                            <FormFile.Label>Json file for Authentication</FormFile.Label>
                            <FormFile.Input required isInvalid onChange={(e) => setFile(e, "file")} />
                        </FormFile>
                        <Button onClick={triggerAttachmentUpload} variant="outline-success">
                            Add Attachments <AiOutlinePlusSquare />
                            <FormFile.Input style={{ display: 'none' }} multiple ref={attachment} size="lg" isInvalid onChange={(e) => setAttachments(e, "attachments")} />
                        </Button>
                        <div style={{
                            display: "flex",
                            flexDirection: "row",
                            flexWrap: "wrap",
                        }}>
                            {state.attachments.map((att, ind) => {
                                return (<Attachment key={ind} filename={att.name} removeAttachment={() => removeAttachment(att.name)} />);
                            })}
                        </div>

                        {spinner}
                        <Button disabled={state.loading} variant="outline-primary" type="button" onClick={sendEmails} style={{marginTop:8}}>
                            Send
                    </Button>
                    </div>
                </Form>
            </Card.Body>
        </Card>
    </div >);
}

export default Main;